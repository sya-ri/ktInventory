package dev.s7a.ktinventory.simplemenu

import dev.s7a.ktinventory.KtInventoryProvider
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SimpleMenu(provider: KtInventoryProvider) {
    private val inventory =
        provider.create("&0&lSelect where to teleport", 1) {
            item(3, ItemStack(Material.RED_BED)) {
                val player = it.whoClicked as? Player ?: return@item
                val bedSpawnLocation = player.bedSpawnLocation
                if (bedSpawnLocation != null) {
                    player.teleport(bedSpawnLocation)
                } else {
                    player.sendMessage("Not found bedSpawnLocation")
                }
                player.closeInventory()
            }
            item(5, ItemStack(Material.COMPASS)) {
                val player = it.whoClicked as? Player ?: return@item
                player.teleport(player.world.spawnLocation)
                player.closeInventory()
            }
        }

    fun open(player: HumanEntity) {
        inventory.open(player)
    }
}
