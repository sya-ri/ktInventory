package dev.s7a.ktinventory

import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
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

    private val storables = mutableSetOf<Storable>()

    fun createButton(
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<KtInventory>) -> Unit,
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
    ) = Storable(this, slots.toList(), save)
        .apply {
            update(initialize())
            storables.add(this)
        }

    final override fun open(player: HumanEntity) {
        KtInventoryHandler.register(plugin)
        player.openInventory(bukkitInventory)
    }

    fun isStorableSlot(slot: Int) = storables.any { it.contains(slot) }

    fun saveStorable() {
        storables.forEach(Storable::save)
    }

    final override fun getInventory() = bukkitInventory

    class Storable(
        val inventory: KtInventory,
        val slots: List<Int>,
        private val save: (List<ItemStack?>) -> Unit,
    ) {
        fun contains(slot: Int) = slots.contains(slot)

        fun update(items: List<ItemStack?>): List<ItemStack?> {
            slots.forEachIndexed { index, slot ->
                val item = items.getOrNull(index)
                inventory.bukkitInventory.setItem(slot, item)
            }
            return items.drop(slots.size)
        }

        fun clear() {
            update(emptyList())
        }

        fun get() = slots.map(inventory.bukkitInventory::getItem)

        fun save() {
            save(get())
        }
    }

    interface StorableType {
        fun allowSave(event: InventoryCloseEvent): Boolean

        data object Default : StorableType {
            override fun allowSave(event: InventoryCloseEvent) = event.viewers.size == 1
        }
    }
}
