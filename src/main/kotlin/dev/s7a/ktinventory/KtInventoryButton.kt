package dev.s7a.ktinventory

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (ClickEvent<@UnsafeVariance T>) -> Unit,
) {
    data class ClickEvent<out T : KtInventoryBase>(
        val inventory: T,
        val player: HumanEntity,
        val click: ClickType,
        val cursor: ItemStack?,
    )

    fun join(onClick: (ClickEvent<@UnsafeVariance T>) -> Unit) =
        KtInventoryButton(itemStack) { state ->
            this.onClick(state)
            onClick(state)
        }
}
