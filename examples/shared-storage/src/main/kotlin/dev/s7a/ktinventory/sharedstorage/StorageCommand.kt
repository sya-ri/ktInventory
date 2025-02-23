package dev.s7a.ktinventory.sharedstorage

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class StorageCommand(
    private val plugin: JavaPlugin,
    private val provider: StorageProvider,
) : CommandExecutor {
    fun register() {
        val command = plugin.getCommand("storage")
        if (command != null) {
            command.setExecutor(this)
        } else {
            throw RuntimeException("storage command not registered")
        }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender is Player) {
            provider.open(sender)
        }
        return true
    }
}
