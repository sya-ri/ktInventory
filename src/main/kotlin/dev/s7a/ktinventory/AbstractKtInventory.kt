package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.options.KtInventoryStorableOption
import dev.s7a.ktinventory.util.getAllViewers
import dev.s7a.ktinventory.util.getOpenInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * Abstract base class for creating inventory-based GUIs.
 * Handles inventory management, button interactions, and item storage functionality.
 *
 * @since 2.0.0
 */
abstract class AbstractKtInventory(
    private val context: KtInventoryPluginContext,
    line: Int,
) : KtInventoryBase(line),
    InventoryHolder {
    /**
     * List of entities currently viewing this inventory
     *
     * @since 2.0.0
     */
    val viewers: List<HumanEntity>
        get() = inventory.viewers

    /**
     * Gets the item at the specified slot
     *
     * @param slot The slot number to get the item from
     * @return The item at the slot, or null if empty
     * @since 2.0.0
     */
    fun getItem(slot: Int) = inventory.getItem(slot)

    /**
     * Sets an item at the specified slot
     *
     * @param slot The slot number to set the item in
     * @param item The item to set, or null to clear
     * @since 2.0.0
     */
    fun setItem(
        slot: Int,
        item: ItemStack?,
    ) = inventory.setItem(slot, item)

    /**
     * Creates a new inventory button with the specified item and click handler
     *
     * @param itemStack The item to display for this button
     * @param onClick The handler for click events on this button
     * @return The created button instance
     * @since 2.0.0
     */
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
        KtInventoryHandler.register(context)
        player.openInventory(inventory)
    }

    private val _storables = mutableSetOf<KtInventoryStorable>()

    /**
     * Set of all storable components in this inventory
     *
     * @since 2.0.0
     */
    val storables
        get() = _storables.toSet()

    /**
     * Gets all storables that have the given slot
     *
     * @param slot The slot number to check
     * @return List of storable components containing this slot
     * @since 2.0.0
     */
    fun getStorables(slot: Int) = storables.filter { it.slots.contains(slot) }

    /**
     * Gets all storables that have any of the given slots
     *
     * @param slots The slot numbers to check
     * @return Distinct list of storable components containing any of these slots
     * @since 2.0.0
     */
    fun getStorables(slots: Iterable<Int>) = storables.filter { it.slots.any(slots::contains) }.distinct()

    /**
     * Storage options for this inventory's storable components
     *
     * @since 2.0.0
     */
    open val storableOption = KtInventoryStorableOption.Default

    /**
     * Add a storable component using all slots in the inventory
     *
     * @param initialize Lambda to provide initial items
     * @param onPreClick Handler called before click events
     * @param onClick Handler for click events
     * @param onPreDrag Handler called before drag events
     * @param onDrag Handler for drag events
     * @param save Handler to save storable state
     * @since 2.0.0
     */
    fun storable(
        initialize: () -> List<ItemStack?> = { emptyList() },
        onPreClick: (KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = { KtInventoryStorable.EventResult.Allow },
        onClick: (KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: (KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = { KtInventoryStorable.EventResult.Allow },
        onDrag: (KtInventoryStorable.DragEvent) -> Unit = {},
        save: (List<ItemStack?>) -> Unit = {},
    ) {
        storable(0 until line * 9, initialize, onPreClick, onClick, onPreDrag, onDrag, save)
    }

    /**
     * Add a storable component with the specified slot numbers
     *
     * @param slots The slot numbers to use
     * @param initialize Lambda to provide initial items
     * @param onPreClick Handler called before click events
     * @param onClick Handler for click events
     * @param onPreDrag Handler called before drag events
     * @param onDrag Handler for drag events
     * @param save Handler to save storable state
     * @since 2.0.0
     */
    fun storable(
        vararg slots: Int,
        initialize: () -> List<ItemStack?> = { emptyList() },
        onPreClick: (KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = { KtInventoryStorable.EventResult.Allow },
        onClick: (KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: (KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = { KtInventoryStorable.EventResult.Allow },
        onDrag: (KtInventoryStorable.DragEvent) -> Unit = {},
        save: (List<ItemStack?>) -> Unit = {},
    ) {
        storable(slots.toList(), initialize, onPreClick, onClick, onPreDrag, onDrag, save)
    }

    /**
     * Add a storable component with the specified slot numbers
     *
     * @param slots The slot numbers to use
     * @param initialize Lambda to provide initial items
     * @param onPreClick Handler called before click events
     * @param onClick Handler for click events
     * @param onPreDrag Handler called before drag events
     * @param onDrag Handler for drag events
     * @param save Handler to save storable state
     * @return The created storable component
     * @since 2.0.0
     */
    fun storable(
        slots: Iterable<Int>,
        initialize: () -> List<ItemStack?> = { emptyList() },
        onPreClick: (KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = { KtInventoryStorable.EventResult.Allow },
        onClick: (KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: (KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = { KtInventoryStorable.EventResult.Allow },
        onDrag: (KtInventoryStorable.DragEvent) -> Unit = {},
        save: (List<ItemStack?>) -> Unit = {},
    ) = KtInventoryStorable(this, slots.toList(), onPreClick, onClick, onPreDrag, onDrag, save)
        .apply {
            update(initialize())
            _storables.add(this)
        }

    /**
     * Saves the state of all storable components in this inventory
     *
     * @since 2.0.0
     */
    fun saveStorables() {
        _storables.forEach(KtInventoryStorable::save)
        storableOption.onSave()
    }

    /**
     * Abstract class for implementing inventory refresh functionality
     *
     * @param T The type of inventory to refresh
     * @param clazz The class of the inventory type
     * @since 2.0.0
     */
    abstract class Refreshable<T : AbstractKtInventory>(
        val clazz: KClass<T>,
    ) : RefreshableInventory<T> {
        /**
         * Creates a new instance of the inventory for refreshing.
         *
         * @param player The player to create the inventory for
         * @param inventory The existing inventory instance
         * @return The new inventory instance, or null to close
         * @since 2.0.0
         */
        abstract fun createNew(
            player: HumanEntity,
            inventory: T,
        ): T?

        final override fun refresh(
            player: HumanEntity,
            predicate: (T) -> Boolean,
        ): Boolean {
            val inventory = getOpenInventory(clazz, player) ?: return false
            if (predicate(inventory).not()) return false
            refresh(player, inventory)
            return true
        }

        final override fun refresh(
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

        final override fun refreshAll(predicate: (Player, T) -> Boolean) {
            getAllViewers(clazz)
                .filter { (player, inventory) ->
                    predicate(player, inventory)
                }.forEach { (player, inventory) ->
                    refresh(player, inventory)
                }
        }
    }
}
