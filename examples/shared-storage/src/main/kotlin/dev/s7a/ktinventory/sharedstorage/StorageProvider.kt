package dev.s7a.ktinventory.sharedstorage

import dev.s7a.ktinventory.KtInventoryProvider
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack

abstract class StorageProvider(inventoryProvider: KtInventoryProvider) {
    abstract var contents: List<ItemStack?>

    private val inventory = inventoryProvider.create("&0&lStorage", 6) {
        onClick {}
        onClose {
            if (it.inventory.viewers.singleOrNull() != null) {
                contents = it.inventory.contents.toList()
            }
        }
        bukkitInventory.contents = contents.toTypedArray()
    }

    fun open(player: HumanEntity) {
        inventory.open(player)
    }
}
