package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.options.KtInventoryStorableOption
import dev.s7a.ktinventory.util.resetableLazy
import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

abstract class KtInventory(
    protected val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line),
    InventoryHolder {
    abstract fun title(): String

    private val bukkitInventoryRef =
        resetableLazy {
            plugin.server.createInventory(this, size, ChatColor.translateAlternateColorCodes('&', title()))
        }

    private val bukkitInventory by bukkitInventoryRef

    val viewers: List<HumanEntity>
        get() = bukkitInventory.viewers

    fun getItem(slot: Int) = bukkitInventory.getItem(slot)

    fun setItem(
        slot: Int,
        item: ItemStack?,
    ) = bukkitInventory.setItem(slot, item)

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

    final override fun open(player: HumanEntity) {
        KtInventoryHandler.register(plugin)
        player.openInventory(bukkitInventory)
    }

    private val _storables = mutableSetOf<KtInventoryStorable>()

    val storables
        get() = _storables.toSet()

    open val storableOption = KtInventoryStorableOption.Default

    fun storable(
        initialize: () -> List<ItemStack?> = { emptyList() },
        canEdit: Boolean = true,
        save: (List<ItemStack?>) -> Unit,
    ) {
        storable(0 until line * 9, initialize, canEdit, save)
    }

    fun storable(
        vararg slots: Int,
        initialize: () -> List<ItemStack?> = { emptyList() },
        canEdit: Boolean = true,
        save: (List<ItemStack?>) -> Unit,
    ) {
        storable(slots.toList(), initialize, canEdit, save)
    }

    fun storable(
        slots: Iterable<Int>,
        initialize: () -> List<ItemStack?> = { emptyList() },
        canEdit: Boolean = true,
        save: (List<ItemStack?>) -> Unit,
    ) = KtInventoryStorable(this, slots.toList(), canEdit, save)
        .apply {
            update(initialize())
            _storables.add(this)
        }

    fun saveStorables() {
        _storables.forEach(KtInventoryStorable::save)
    }

    override fun refresh() {
        val viewers = this.viewers
        bukkitInventoryRef.reset()
        viewers.forEach { player ->
            open(player)
        }
    }

    final override fun getInventory() = bukkitInventory
}
