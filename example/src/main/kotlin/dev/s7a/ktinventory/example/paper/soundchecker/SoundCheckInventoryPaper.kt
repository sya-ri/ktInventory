package dev.s7a.ktinventory.example.paper.soundchecker

import dev.s7a.ktinventory.KtInventoryPaginatedAdventure
import dev.s7a.ktinventory.KtInventoryPluginContext
import dev.s7a.ktinventory.example.itemStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SoundCheckInventoryPaper(
    context: KtInventoryPluginContext,
) : KtInventoryPaginatedAdventure(context, 6) {
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
    ) = Component.text("Sound checker (${page + 1}/${lastPage + 1})").color(NamedTextColor.BLACK).decorate(TextDecoration.BOLD)

    init {
        paginateSlot(0 until 45)
        previousPageButton(45, itemStack(Material.ARROW, "&d<<"))
        nextPageButton(53, itemStack(Material.ARROW, "&d>>"))
    }
}
