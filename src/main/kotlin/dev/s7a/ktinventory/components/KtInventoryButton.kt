package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.KtInventoryBase
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (ClickEvent<@UnsafeVariance T>) -> Unit,
) {
    class ClickEvent<out T : KtInventoryBase>(
        val inventory: T,
        private val event: InventoryClickEvent,
    ) {
        @Deprecated("Use wrapper")
        fun unsafe() = event

        val player
            get() = event.whoClicked

        val click
            get() = event.click

        val hotbarButton
            get() = event.hotbarButton

        val slot
            get() = event.slot
    }

    fun join(onClick: (ClickEvent<T>) -> Unit) =
        KtInventoryButton(itemStack) { state ->
            this.onClick(state)
            onClick(state)
        }
}
