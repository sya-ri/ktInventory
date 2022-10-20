package dev.s7a.ktinventory.sharedstorage

import dev.s7a.ktinventory.ktInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class StorageCommand(private val plugin: JavaPlugin, private val provider: StorageProvider) : CommandExecutor {
    fun register() {
        val command = plugin.getCommand("storage")
        if (command != null) {
            command.setExecutor(this)
        } else {
            throw RuntimeException("storage command not registered")
        }
    }

    private val inventory = plugin.ktInventory("&0&lStorage", 6) {
        onClick {}
        onClose {
            if (it.inventory.viewers.singleOrNull() != null) {
                provider.contents = it.inventory.contents.toList()
            }
        }
        bukkitInventory.contents = provider.contents.toTypedArray()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            inventory.open(sender)
        }
        return true
    }
}
