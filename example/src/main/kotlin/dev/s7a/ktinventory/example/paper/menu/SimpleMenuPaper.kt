package dev.s7a.ktinventory.example.paper.menu

import dev.s7a.ktinventory.KtInventoryAdventure
import dev.s7a.ktinventory.example.itemStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SimpleMenuPaper(
    plugin: Plugin,
) : KtInventoryAdventure(plugin, 1) {
    override fun title() = Component.text("Select where to teleport").color(NamedTextColor.BLACK).decorate(TextDecoration.BOLD)

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
