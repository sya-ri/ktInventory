package dev.s7a.ktinventory.example

import dev.s7a.ktinventory.example.menu.SimpleMenu
import dev.s7a.ktinventory.example.soundchecker.SoundCheckInventory
import dev.s7a.ktinventory.example.storage.StorageInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ExamplePlugin : JavaPlugin() {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (label == "example") {
            if (sender !is Player) return false
            when (args.getOrNull(0)) {
                "menu" -> {
                    SimpleMenu(this).open(sender)
                    return true
                }
                "sound" -> {
                    SoundCheckInventory(this).open(sender)
                    return true
                }
                "storage" -> {
                    StorageInventory(this).open(sender)
                    return true
                }
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String>? =
        when (args.size) {
            1 -> listOf("menu", "sound", "storage").filter { it.startsWith(args[0]) }
            else -> null
        }
}
