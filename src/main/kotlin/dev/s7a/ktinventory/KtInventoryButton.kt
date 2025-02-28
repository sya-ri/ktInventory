package dev.s7a.ktinventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (InventoryClickEvent, @UnsafeVariance T) -> Unit,
) {
    fun join(onClick: (InventoryClickEvent, T) -> Unit) =
        KtInventoryButton<T>(itemStack) { event, inventory ->
            this.onClick(event, inventory)
            onClick(event, inventory)
        }
}
