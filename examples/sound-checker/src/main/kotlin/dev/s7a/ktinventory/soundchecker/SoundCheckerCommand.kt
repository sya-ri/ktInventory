package dev.s7a.ktinventory.soundchecker

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

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender is Player) {
            SoundCheckInventory(plugin).open(sender)
        }
        return true
    }
}
