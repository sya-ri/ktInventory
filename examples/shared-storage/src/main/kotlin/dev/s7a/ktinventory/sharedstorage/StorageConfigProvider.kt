package dev.s7a.ktinventory.sharedstorage

import dev.s7a.ktinventory.KtInventoryProvider
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class StorageConfigProvider(private val plugin: JavaPlugin) : StorageProvider(KtInventoryProvider(plugin)) {
    override var contents: List<ItemStack?>
        get() = plugin.config.getList("contents").orEmpty().filterIsInstance<ItemStack?>()
        set(value) {
            plugin.config.set("contents", value)
            plugin.saveConfig()
        }
}
