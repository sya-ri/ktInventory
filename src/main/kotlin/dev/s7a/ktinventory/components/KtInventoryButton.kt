package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.KtInventoryBase
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (ClickEvent<@UnsafeVariance T>) -> Unit,
) : KtInventoryComponent() {
    class ClickEvent<out T : KtInventoryBase>(
        val inventory: T,
        private val event: InventoryClickEvent,
    ) {
        @Deprecated("Use wrapper")
        fun unsafe() = event

        val player
            get() = event.whoClicked

        val slotType
            get() = event.slotType

        val cursor
            get() = event.cursor

        val currentItem
            get() = event.currentItem

        val slot
            get() = event.slot

        val hotbarButton
            get() = event.hotbarButton

        val action
            get() = event.action

        val click
            get() = event.click
    }

    fun join(onClick: (ClickEvent<T>) -> Unit) =
        KtInventoryButton(itemStack) { state ->
            this.onClick(state)
            onClick(state)
        }
}
