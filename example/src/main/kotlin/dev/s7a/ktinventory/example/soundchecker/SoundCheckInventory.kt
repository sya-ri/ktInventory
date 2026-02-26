package dev.s7a.ktinventory.example.soundchecker

import dev.s7a.ktinventory.KtInventoryPaginated
import dev.s7a.ktinventory.KtInventoryPluginContext
import dev.s7a.ktinventory.example.itemStack
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SoundCheckInventory(
    context: KtInventoryPluginContext,
) : KtInventoryPaginated(context, 6) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override val entries =
        Registry.SOUNDS.map { sound ->
            createButton(
                itemStack(Material.GRAY_DYE, "&6${sound.key.key}"),
            ) { event ->
                val player = event.player as? Player ?: return@createButton
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
        nextPageButton(53, itemStack(Material.ARROW, "&d>>"))
    }
}
