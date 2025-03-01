package dev.s7a.ktinventory

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

internal class KtInventoryHandler(
    private val plugin: Plugin,
) : Listener {
    @EventHandler
    fun on(event: InventoryOpenEvent) {
        val inventory = event.inventory.holder as? KtInventory ?: return
        inventory.onOpen(event)
    }

    private fun KtInventory.isCancelClick(slot: Int): Boolean {
        if (size <= slot) return false

        return isStorableSlot(slot).not()
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val inventory = event.inventory.holder as? KtInventory ?: return

        if (inventory.inventory === event.clickedInventory) {
            if (inventory.isCancelClick(event.slot)) {
                event.isCancelled = true
            }

            inventory.handleClick(event)
            inventory.onClick(event)
        }
    }

    @EventHandler
    fun on(event: InventoryDragEvent) {
        val inventory = event.inventory.holder as? KtInventory ?: return
        if (event.rawSlots.any { inventory.isCancelClick(it) }) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        val inventory = event.inventory.holder as? KtInventory ?: return
        inventory.onClose(event)
        if (inventory.storableType.allowSave(event)) {
            inventory.saveStorable()
        }
    }

    @EventHandler
    fun on(event: PluginDisableEvent) {
        if (plugin === event.plugin) {
            Bukkit.getOnlinePlayers().forEach { player ->
                val inventory = player.openInventory.topInventory
                if (inventory.holder !is KtInventory) return@forEach
                player.closeInventory()
            }
            handlers.remove(plugin)
        }
    }

    companion object {
        private val handlers = mutableMapOf<Plugin, KtInventoryHandler>()

        @Synchronized
        fun register(plugin: Plugin) =
            handlers.getOrPut(plugin) {
                KtInventoryHandler(plugin).apply {
                    plugin.server.pluginManager.registerEvents(this, plugin)
                }
            }
    }
}
