package dev.s7a.ktinventory.sharedstorage

import dev.s7a.ktinventory.ktInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

abstract class StorageProvider(plugin: JavaPlugin) {
    abstract var contents: List<ItemStack?>

    private val inventory = plugin.ktInventory("&0&lStorage", 6) {
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
