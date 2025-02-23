package dev.s7a.ktinventory.sharedstorage

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class StorageConfigProvider(
    plugin: JavaPlugin,
) : StorageProvider(plugin) {
    override var contents: List<ItemStack?>
        get() =
            plugin.config
                .getList("contents")
                .orEmpty()
                .filterIsInstance<ItemStack?>()
        set(value) {
            plugin.config.set("contents", value)
            plugin.saveConfig()
        }
}
