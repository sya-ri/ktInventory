package dev.s7a.ktinventory.complexmenu

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ComplexMenuCommand(
    private val plugin: JavaPlugin,
    private val menu: ComplexMenu,
) : CommandExecutor {
    fun register() {
        val command = plugin.getCommand("complex-menu")
        if (command != null) {
            command.setExecutor(this)
        } else {
            throw RuntimeException("complex-menu command not registered")
        }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender is Player) {
            menu.open(sender)
        }
        return true
    }
}
