package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.options.KtInventoryStorableOption
import dev.s7a.ktinventory.util.getAllViewers
import dev.s7a.ktinventory.util.getOpenInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

abstract class AbstractKtInventory(
    private val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line),
    InventoryHolder {
    val viewers: List<HumanEntity>
        get() = inventory.viewers

    fun getItem(slot: Int) = inventory.getItem(slot)

    fun setItem(
        slot: Int,
        item: ItemStack?,
    ) = inventory.setItem(slot, item)

    fun createButton(
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<AbstractKtInventory>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        super.button(slot, item)
        inventory.setItem(slot, item.itemStack)
    }

    final override fun open(player: HumanEntity) {
        KtInventoryHandler.register(plugin)
        player.openInventory(inventory)
    }

    private val _storables = mutableSetOf<KtInventoryStorable>()

    val storables
        get() = _storables.toSet()

    open val storableOption = KtInventoryStorableOption.Default

    fun storable(
        initialize: () -> List<ItemStack?> = { emptyList() },
        canEdit: Boolean = true,
        onClick: (InventoryClickEvent) -> Unit = {},
        save: (List<ItemStack?>) -> Unit = {},
    ) {
        storable(0 until line * 9, initialize, canEdit, onClick, save)
    }

    fun storable(
        vararg slots: Int,
        initialize: () -> List<ItemStack?> = { emptyList() },
        canEdit: Boolean = true,
        onClick: (InventoryClickEvent) -> Unit = {},
        save: (List<ItemStack?>) -> Unit = {},
    ) {
        storable(slots.toList(), initialize, canEdit, onClick, save)
    }

    fun storable(
        slots: Iterable<Int>,
        initialize: () -> List<ItemStack?> = { emptyList() },
        canEdit: Boolean = true,
        onClick: (InventoryClickEvent) -> Unit = {},
        save: (List<ItemStack?>) -> Unit = {},
    ) = KtInventoryStorable(this, slots.toList(), canEdit, onClick, save)
        .apply {
            update(initialize())
            _storables.add(this)
        }

    fun saveStorables() {
        _storables.forEach(KtInventoryStorable::save)
    }

    abstract class Refreshable<T : AbstractKtInventory>(
        val clazz: KClass<T>,
    ) {
        abstract fun createNew(
            player: HumanEntity,
            inventory: T,
        ): T?

        fun refresh(
            player: HumanEntity,
            predicate: (T) -> Boolean = { true },
        ): Boolean {
            val inventory = getOpenInventory(clazz, player) ?: return false
            if (predicate(inventory).not()) return false
            refresh(player, inventory)
            return true
        }

        fun refresh(
            player: HumanEntity,
            inventory: T,
        ) {
            val newInventory = createNew(player, inventory)
            if (newInventory != null) {
                newInventory.open(player)
            } else {
                player.closeInventory()
            }
        }

        fun refreshAll(predicate: (Player, T) -> Boolean = { _, _ -> true }) {
            getAllViewers(clazz)
                .filter { (player, inventory) ->
                    predicate(player, inventory)
                }.forEach { (player, inventory) ->
                    refresh(player, inventory)
                }
        }
    }
}
