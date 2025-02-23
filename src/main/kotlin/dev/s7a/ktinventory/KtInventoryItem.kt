package dev.s7a.ktinventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class KtInventoryItem<out T : KtInventory>(
    val itemStack: ItemStack,
    val onClick: (InventoryClickEvent, @UnsafeVariance T) -> Unit,
) {
    fun join(onClick: (InventoryClickEvent, T) -> Unit) =
        KtInventoryItem<T>(itemStack) { event, inventory ->
            this.onClick(event, inventory)
            onClick(event, inventory)
        }
}
