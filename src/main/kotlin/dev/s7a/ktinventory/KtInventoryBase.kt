package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack

/**
 * Base class for creating custom inventories.
 *
 * @property line Number of rows in the inventory (must be between 1 and 6)
 * @since 2.0.0
 */
abstract class KtInventoryBase(
    val line: Int,
) {
    init {
        require(line in 1..6) { "line must be between 1 and 6" }
    }

    /**
     * Total size of inventory (line * 9)
     *
     * @since 2.0.0
     */
    val size: Int
        get() = line * 9

    private val _buttons = mutableMapOf<Int, KtInventoryButton<KtInventoryBase>>()

    /**
     * Map of all buttons in the inventory
     *
     * @since 2.0.0
     */
    val buttons
        get() = _buttons.toMap()

    /**
     * Adds a button to multiple slots with the given item and click handler
     *
     * @param slot Slot numbers to place the button
     * @param itemStack Item to display
     * @param onClick Click event handler
     * @since 2.0.0
     */
    fun button(
        vararg slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<AbstractKtInventory>) -> Unit = {},
    ) {
        button(slot.toList(), itemStack, onClick)
    }

    /**
     * Adds a button to a single slot with the given item and click handler
     *
     * @param slot Slot number to place the button
     * @param itemStack Item to display
     * @param onClick Click event handler
     * @since 2.0.0
     */
    fun button(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<AbstractKtInventory>) -> Unit = {},
    ) {
        button(slot, KtInventoryButton(itemStack, onClick))
    }

    /**
     * Adds a button to multiple slots with the given item and click handler
     *
     * @param slots Slot numbers to place the button
     * @param itemStack Item to display
     * @param onClick Click event handler
     * @since 2.0.0
     */
    fun button(
        slots: Iterable<Int>,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<AbstractKtInventory>) -> Unit = {},
    ) {
        slots.forEach { slot ->
            button(slot, KtInventoryButton(itemStack, onClick))
        }
    }

    /**
     * Adds an existing button to a single slot
     *
     * @param slot Slot number to place the button
     * @param item Button to add
     * @throws IllegalArgumentException if slot is not between 0 and size
     * @since 2.0.0
     */
    open fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        require(slot in 0 until size) { "slot must be between 0 and $size (actual: $slot)" }
        _buttons[slot] = item
    }

    /**
     * Adds an existing button to multiple slots
     *
     * @param slot Slot numbers to place the button
     * @param item Button to add
     * @since 2.0.0
     */
    fun button(
        vararg slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        button(slot.toList(), item)
    }

    /**
     * Adds an existing button to multiple slots
     *
     * @param slots Slot numbers to place the button
     * @param item Button to add
     * @since 2.0.0
     */
    fun button(
        slots: Iterable<Int>,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        slots.forEach { slot ->
            button(slot, item)
        }
    }

    /**
     * Handles click events for buttons in this inventory
     *
     * @param event The inventory click event
     * @since 2.0.0
     */
    internal fun handleClick(event: InventoryClickEvent) {
        val slot = event.slot
        val button = buttons[slot]
        if (button != null) {
            button.onClick(KtInventoryButton.ClickEvent(this, event))
        }
    }

    /**
     * Called when inventory is opened
     *
     * @param event The inventory open event
     * @since 2.0.0
     */
    open fun onOpen(event: InventoryOpenEvent) {}

    /**
     * Called when top inventory is clicked
     *
     * @param event The inventory click event
     * @since 2.0.0
     */
    open fun onClick(event: InventoryClickEvent) {}

    /**
     * Called when bottom inventory is clicked
     *
     * @param event The inventory click event
     * @since 2.0.0
     */
    open fun onClickBottom(event: InventoryClickEvent) {}

    /**
     * Called when items are dragged in inventory
     *
     * @param event The inventory drag event
     * @since 2.0.0
     */
    open fun onDrag(event: InventoryDragEvent) {}

    /**
     * Called when inventory is closed
     *
     * @param event The inventory close event
     * @since 2.0.0
     */
    open fun onClose(event: InventoryCloseEvent) {}

    /**
     * Opens this inventory for the given player
     *
     * @param player Player to open inventory for
     * @since 2.0.0
     */
    abstract fun open(player: HumanEntity)
}
