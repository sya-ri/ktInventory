package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.KtInventoryBase
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Represents a clickable button in a KtInventory.
 *
 * @param T The type of inventory this button belongs to
 * @property itemStack The item to display for this button
 * @property onClick Callback function executed when button is clicked
 * @since 2.0.0
 */
class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (ClickEvent<@UnsafeVariance T>) -> Unit,
) : KtInventoryComponent() {
    /**
     * Contains information about a button click event.
     *
     * @param T The type of inventory where the click occurred
     * @property inventory The inventory instance where the click occurred
     * @property event The underlying Bukkit inventory click event
     * @since 2.0.0
     */
    class ClickEvent<out T : KtInventoryBase>(
        val inventory: T,
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
         * The player who clicked
         *
         * @since 2.0.0
         */
        val player
            get() = event.whoClicked

        /**
         * The type of slot that was clicked
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
         * The item currently in the clicked slot
         *
         * @since 2.0.0
         */
        val currentItem
            get() = event.currentItem

        /**
         * The slot number that was clicked
         *
         * @since 2.0.0
         */
        val slot
            get() = event.slot

        /**
         * The number of the clicked hotbar button (0-8) or -1 if not a NUMBER_KEY action
         *
         * @since 2.0.0
         */
        val hotbarButton
            get() = event.hotbarButton

        /**
         * The click action performed
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

    fun join(onClick: (ClickEvent<T>) -> Unit) =
        KtInventoryButton(itemStack) { state ->
            this.onClick(state)
            onClick(state)
        }
}
