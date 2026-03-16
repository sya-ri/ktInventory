package dev.s7a.ktinventory.example.storage

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPluginContext
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class StorageInventory(
    plugin: Plugin,
) : KtInventory(KtInventoryPluginContext(plugin), 6) {
    override fun title() = "&0&lStorage"

    init {
        storable({
            plugin.config
                .getList("contents")
                .orEmpty()
                .filterIsInstance<ItemStack?>()
        }) { items ->
            Bukkit.broadcastMessage(items.filterNotNull().joinToString { "${it.type.name} x${it.amount}" })
            plugin.config.set("contents", items)
            plugin.saveConfig()
        }
    }
}
