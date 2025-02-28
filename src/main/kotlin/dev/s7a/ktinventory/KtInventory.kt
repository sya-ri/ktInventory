package dev.s7a.ktinventory

import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

abstract class KtInventory(
    protected val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line),
    InventoryHolder {
    abstract fun title(): String

    private val bukkitInventory by lazy {
        plugin.server.createInventory(this, line * 9, ChatColor.translateAlternateColorCodes('&', title()))
    }

    private val storables = mutableMapOf<List<Int>, (List<ItemStack?>) -> Unit>()

    fun createButton(
        itemStack: ItemStack,
        onClick: (InventoryClickEvent, KtInventory) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        super.button(slot, item)
        bukkitInventory.setItem(slot, item.itemStack)
    }

    open val storableType: StorableType = StorableType.Default

    fun storable(
        initialize: () -> List<ItemStack?> = { emptyList() },
        save: (List<ItemStack?>) -> Unit,
    ) {
        storable(0 until line * 9, initialize, save)
    }

    fun storable(
        vararg slots: Int,
        initialize: () -> List<ItemStack?> = { emptyList() },
        save: (List<ItemStack?>) -> Unit,
    ) {
        storable(slots.toList(), initialize, save)
    }

    fun storable(
        slots: Iterable<Int>,
        initialize: () -> List<ItemStack?> = { emptyList() },
        save: (List<ItemStack?>) -> Unit,
    ) {
        storables[slots.toList()] = save
        val items = initialize()
        val lastIndex = items.lastIndex
        slots.forEachIndexed { index, slot ->
            if (lastIndex < index) return
            val item = items[index]
            bukkitInventory.setItem(slot, item)
        }
    }

    final override fun open(player: HumanEntity) {
        KtInventoryHandler.register(plugin)
        player.openInventory(bukkitInventory)
    }

    fun saveStorable() {
        storables.forEach { (slots, save) ->
            save(slots.map(bukkitInventory::getItem))
        }
    }

    final override fun getInventory() = bukkitInventory

    interface StorableType {
        fun allowSave(event: InventoryCloseEvent): Boolean

        data object Default : StorableType {
            override fun allowSave(event: InventoryCloseEvent) = event.viewers.size == 1
        }
    }
}
