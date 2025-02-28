package dev.s7a.ktinventory

import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class KtInventoryButton<out T : KtInventoryBase> internal constructor(
    val itemStack: ItemStack,
    val onClick: (InventoryClickEvent, @UnsafeVariance T) -> Unit,
) {
    fun join(onClick: (InventoryClickEvent, T) -> Unit) =
        KtInventoryButton<T>(itemStack) { event, inventory ->
            this.onClick(event, inventory)
            onClick(event, inventory)
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
