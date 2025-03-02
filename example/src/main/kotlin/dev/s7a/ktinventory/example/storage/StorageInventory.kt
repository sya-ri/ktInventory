package dev.s7a.ktinventory.example.storage

import dev.s7a.ktinventory.KtInventory
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.text.orEmpty
import kotlin.text.set

class StorageInventory(
    plugin: Plugin,
) : KtInventory(plugin, 6) {
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
