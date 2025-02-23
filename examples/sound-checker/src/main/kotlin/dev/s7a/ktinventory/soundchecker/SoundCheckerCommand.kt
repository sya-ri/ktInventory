package dev.s7a.ktinventory.soundchecker

import dev.s7a.ktinventory.ktInventory
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SoundCheckerCommand(
    private val plugin: JavaPlugin,
) : CommandExecutor {
    fun register() {
        val command = plugin.getCommand("sound-checker")
        if (command != null) {
            command.setExecutor(this)
        } else {
            throw RuntimeException("sound-checker command not registered")
        }
    }

    private val pagedSounds = Sound.values().toList().chunked(45)
    private val lastPage = pagedSounds.lastIndex

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender is Player) {
            openPage(sender, 0)
        }
        return true
    }

    private fun openPage(
        player: Player,
        page: Int,
    ) {
        when {
            page < 0 -> openPage(player, 0)
            lastPage < page -> openPage(player, lastPage)
            else -> {
                plugin
                    .ktInventory("&0&lSound checker (${page + 1}/${lastPage + 1})", 6) {
                        pagedSounds[page].forEachIndexed { index, sound ->
                            item(index, Material.GRAY_DYE, "&6${sound.key.key}") {
                                player.playSound(player.location, sound, 1F, 1F)
                            }
                        }
                        item(45, Material.ARROW, "&d<<") {
                            openPage(player, page - 1)
                        }
                        item(53, Material.ARROW, "&d>>") {
                            openPage(player, page + 1)
                        }
                    }.open(player)
            }
        }
    }
}
