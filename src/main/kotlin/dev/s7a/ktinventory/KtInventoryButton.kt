package dev.s7a.ktinventory

import org.bukkit.NamespacedKey
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (ClickState<@UnsafeVariance T>) -> Unit,
) {
    data class ClickState<out T : KtInventoryBase>(
        val inventory: T,
        val player: HumanEntity,
        val click: ClickType,
        val cursor: ItemStack?,
    )

    fun join(onClick: (ClickState<@UnsafeVariance T>) -> Unit) =
        KtInventoryButton(itemStack) { state ->
            this.onClick(state)
            onClick(state)
        }

    val icon: ItemStack
        get() =
            itemStack.clone().apply {
                itemMeta =
                    itemMeta?.apply {
                        persistentDataContainer.set(key, PersistentDataType.BYTE, 1.toByte())
                    }
            }

    companion object {
        @Suppress("UnstableApiUsage")
        private val key = NamespacedKey("ktinventory", "internal")
    }
}
