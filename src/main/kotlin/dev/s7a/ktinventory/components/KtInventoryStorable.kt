package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.AbstractKtInventory
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

/**
 * Represents a storable component of a KtInventory that manages inventory slots and their interactions.
 *
 * @property inventory The parent KtInventory instance
 * @property slots List of slot indices managed by this component
 * @property onPreClick Callback invoked before click events to determine if action should proceed
 * @property onClick Callback invoked when slots are clicked
 * @property onPreDrag Callback invoked before drag events to determine if action should proceed
 * @property onDrag Callback invoked when items are dragged
 * @since 2.0.0
 */
class KtInventoryStorable internal constructor(
    val inventory: AbstractKtInventory,
    val slots: List<Int>,
    val onPreClick: (ClickEvent) -> EventResult,
    val onClick: (ClickEvent) -> Unit,
    val onPreDrag: (DragEvent) -> EventResult,
    val onDrag: (DragEvent) -> Unit,
    private val save: (List<ItemStack?>) -> Unit,
) : KtInventoryComponent() {
    /**
     * Checks if this component manages the specified slot.
     *
     * @param slot The slot index to check
     * @return true if the slot is managed by this component
     * @since 2.0.0
     */
    fun contains(slot: Int) = slots.contains(slot)

    /**
     * Updates the items in managed slots with the provided list of items.
     *
     * @param items List of items to set in the managed slots
     * @return Remaining items that couldn't fit in the managed slots
     * @since 2.0.0
     */
    fun update(items: List<ItemStack?>): List<ItemStack?> {
        slots.forEachIndexed { index, slot ->
            val item = items.getOrNull(index)
            inventory.setItem(slot, item)
        }
        return items.drop(slots.size)
    }

    /**
     * Clears all items from managed slots.
     *
     * @since 2.0.0
     */
    fun clear() {
        update(emptyList())
    }

    /**
     * Gets all items from managed slots.
     *
     * @return List of items in managed slots (null represents empty slots)
     * @since 2.0.0
     */
    fun get() =
        slots.map(inventory::getItem).map {
            // AIR -> null
            it?.takeUnless { it.type.isAir }
        }

    /**
     * Saves the current state of items in managed slots.
     * @since 2.0.0
     */
    fun save() {
        save(get())
    }

    /**
     * Wrapper class for inventory click events.
     *
     * @property player The player who triggered the event
     * @property slotType The type of slot clicked
     * @property cursor The item on the cursor
     * @property currentItem The item in the clicked slot
     * @property slot The clicked slot number
     * @property hotbarButton The hotbar button pressed
     * @property action The inventory action performed
     * @property click The click type
     * @since 2.0.0
     */
    class ClickEvent(
        private val event: InventoryClickEvent,
    ) {
        /**
         * Gets the underlying Bukkit inventory click event.
         *
         * @return The raw InventoryClickEvent
         * @since 2.0.0
         */
        @Deprecated("Use wrapper")
        fun unsafe() = event

        /**
         * The player who triggered the event
         *
         * @since 2.0.0
         */
        val player
            get() = event.whoClicked

        /**
         * The type of slot clicked
         *
         * @since 2.0.0
         */
        val slotType
            get() = event.slotType

        /**
         * The item on the cursor
         *
         * @since 2.0.0
         */
        val cursor
            get() = event.cursor

        /**
         * The item in the clicked slot
         *
         * @since 2.0.0
         */
        val currentItem
            get() = event.currentItem

        /**
         * The clicked slot number
         *
         * @since 2.0.0
         */
        val slot
            get() = event.slot

        /**
         * The hotbar button pressed
         *
         * @since 2.0.0
         */
        val hotbarButton
            get() = event.hotbarButton

        /**
         * The inventory action performed
         *
         * @since 2.0.0
         */
        val action
            get() = event.action

        /**
         * The click type
         *
         * @since 2.0.0
         */
        val click
            get() = event.click
    }

    /**
     * Wrapper class for inventory drag events.
     *
     * @property player The player who triggered the event
     * @property newItems Map of slot numbers to items being added
     * @property rawSlots Set of raw slot numbers affected
     * @property slots Set of inventory slot numbers affected
     * @property cursor The item on the cursor
     * @property oldCursor The previous cursor item
     * @property type The drag type
     * @since 2.0.0
     */
    class DragEvent(
        private val event: InventoryDragEvent,
    ) {
        /**
         * Gets the underlying Bukkit inventory drag event.
         *
         * @return The raw InventoryDragEvent
         * @since 2.0.0
         */
        @Deprecated("Use wrapper")
        fun unsafe() = event

        /**
         * The player who triggered the event
         *
         * @since 2.0.0
         */
        val player
            get() = event.whoClicked

        /**
         * Map of slot numbers to items being added
         *
         * @since 2.0.0
         */
        val newItems
            get() = event.newItems

        /**
         * Set of raw slot numbers affected
         *
         * @since 2.0.0
         */
        val rawSlots
            get() = event.rawSlots

        /**
         * Set of inventory slot numbers affected
         *
         * @since 2.0.0
         */
        val slots
            get() = event.inventorySlots

        /**
         * The item on the cursor
         *
         * @since 2.0.0
         */
        val cursor
            get() = event.cursor

        /**
         * The previous cursor item
         *
         * @since 2.0.0
         */
        val oldCursor
            get() = event.oldCursor

        /**
         * The drag type
         *
         * @since 2.0.0
         */
        val type
            get() = event.type
    }

    /**
     * Represents the result of handling an inventory event.
     *
     * @property Allow The event should be allowed to proceed
     * @property Deny The event should be cancelled
     * @since 2.0.0
     */
    enum class EventResult {
        Allow,
        Deny,
    }
}
