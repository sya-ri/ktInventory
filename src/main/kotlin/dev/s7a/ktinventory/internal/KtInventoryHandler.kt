package dev.s7a.ktinventory.internal

import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

internal class KtInventoryHandler(private val plugin: Plugin) : Listener {
    private val preOpenInventories = mutableMapOf<UUID, KtInventoryImpl>()
    private val openInventories = mutableMapOf<UUID, KtInventoryImpl>()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun open(player: HumanEntity, inventory: KtInventoryImpl) {
        preOpenInventories[player.uniqueId] = inventory
        player.openInventory(inventory.bukkitInventory)
        openInventories[player.uniqueId] = inventory
    }

    @EventHandler
    fun on(event: InventoryOpenEvent) {
        val player = event.player
        val inventory = preOpenInventories.remove(player.uniqueId) ?: return
        if (inventory.bukkitInventory === event.inventory) {
            inventory.onOpen?.invoke(event)
        }
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked
        val inventory = openInventories[player.uniqueId] ?: return
        if (inventory.bukkitInventory !== event.inventory) {
            openInventories.remove(player.uniqueId)
            return
        }
        inventory.onClick?.invoke(event)
        if (inventory.bukkitInventory === event.clickedInventory) {
            inventory.actions[event.slot]?.invoke(event)
        }
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        val player = event.player
        val inventory = openInventories.remove(player.uniqueId) ?: return
        if (inventory.bukkitInventory === event.inventory) {
            inventory.onClose?.invoke(event)
        }
    }

    companion object {
        private val handlers = mutableMapOf<Plugin, KtInventoryHandler>()

        @Synchronized
        fun get(plugin: Plugin) = handlers.getOrPut(plugin) { KtInventoryHandler(plugin) }
    }
}
