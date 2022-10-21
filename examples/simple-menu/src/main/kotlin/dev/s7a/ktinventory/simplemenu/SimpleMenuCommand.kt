package dev.s7a.ktinventory.simplemenu

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SimpleMenuCommand(private val plugin: JavaPlugin, private val menu: SimpleMenu) : CommandExecutor {
    fun register() {
        val command = plugin.getCommand("simple-menu")
        if (command != null) {
            command.setExecutor(this)
        } else {
            throw RuntimeException("simple-menu command not registered")
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            menu.open(sender)
        }
        return true
    }
}
