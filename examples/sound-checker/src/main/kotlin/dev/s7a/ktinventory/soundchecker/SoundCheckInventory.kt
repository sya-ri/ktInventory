package dev.s7a.ktinventory.soundchecker

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryItem
import dev.s7a.ktinventory.soundchecker.utils.itemStack
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SoundCheckInventory(
    plugin: Plugin,
) : KtInventory.Paginated(plugin, 6) {
    override val entries =
        Sound.values().map { sound ->
            KtInventoryItem<Entry>(
                itemStack(Material.GRAY_DYE, "&6${sound.key.key}"),
            ) { event, _ ->
                val player = event.whoClicked as? Player ?: return@KtInventoryItem
                player.playSound(player.location, sound, 1F, 1F)
            }
        }

    override fun title(
        page: Int,
        lastPage: Int,
    ): String = "&0&lSound checker (${page + 1}/${lastPage + 1})"

    init {
        paginateSlot(0 until 45)
        previousPageButton(45, itemStack(Material.ARROW, "&d<<"))
        nextPageButton(45, itemStack(Material.ARROW, "&d<<"))
    }
}
