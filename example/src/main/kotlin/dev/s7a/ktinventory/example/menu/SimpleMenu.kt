package dev.s7a.ktinventory.example.menu

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPluginContext
import dev.s7a.ktinventory.example.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SimpleMenu(
    context: KtInventoryPluginContext,
) : KtInventory(context, 1) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override fun title() = "&0&lSelect where to teleport"

    init {
        button(3, itemStack(Material.RED_BED, "&cRespawn")) { event ->
            val player = event.player as? Player ?: return@button
            val respawnLocation = player.bedSpawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found bedSpawnLocation")
            }
            player.closeInventory()
        }
        button(5, itemStack(Material.COMPASS, "&bWorldSpawn")) { event ->
            val player = event.player as? Player ?: return@button
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }
}
