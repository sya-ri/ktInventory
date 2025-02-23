package dev.s7a.ktinventory.sharedstorage

import dev.s7a.ktinventory.KtInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

abstract class StorageProvider(
    plugin: Plugin,
) : KtInventory(plugin, 6) {
    override fun title() = "&0&lStorage"

    abstract var contents: List<ItemStack?>

    init {
        // FIXME storage
    }
}
