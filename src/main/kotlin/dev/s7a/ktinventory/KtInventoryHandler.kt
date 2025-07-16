package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryStorable
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
        val inventory = event.inventory.holder as? AbstractKtInventory ?: return
        inventory.onOpen(event)
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val inventory = event.inventory.holder as? AbstractKtInventory ?: return

        if (inventory.inventory === event.clickedInventory) {
            // Storable OnPreClick
            val storables = inventory.getStorables(event.slot)
            val storableClickEvent = KtInventoryStorable.ClickEvent(event)
            if (storables.map { it.onPreClick(storableClickEvent) }.contains(KtInventoryStorable.EventResult.Deny)) {
                event.isCancelled = true
            }

            // Button
            inventory.handleClick(event)

            // Storable OnClick
            storables.forEach { storable ->
                storable.onClick(storableClickEvent)
            }

            // Inventory OnClick
            inventory.onClick(event)
        } else {
            inventory.onClickBottom(event)
        }
    }

    @EventHandler
    fun on(event: InventoryDragEvent) {
        val inventory = event.inventory.holder as? AbstractKtInventory ?: return

        // Storable PreDrag
        val storables = inventory.getStorables(event.rawSlots)
        val storableDragEvent = KtInventoryStorable.DragEvent(event)
        if (storables.map { it.onPreDrag(storableDragEvent) }.contains(KtInventoryStorable.EventResult.Deny)) {
            event.isCancelled = true
        }

        // Storable Drag
        storables.forEach { storable ->
            storable.onDrag(storableDragEvent)
        }

        // Inventory OnDrag
        inventory.onDrag(event)
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        val inventory = event.inventory.holder as? AbstractKtInventory ?: return

        // Storable Save
        if (inventory.storableOption.allowSave(event)) {
            inventory.saveStorables()
        }

        // Inventory OnClose
        inventory.onClose(event)
    }

    @EventHandler
    fun on(event: PluginDisableEvent) {
        if (plugin === event.plugin) {
            Bukkit.getOnlinePlayers().forEach { player ->
                val inventory = player.openInventory.topInventory
                if (inventory.holder !is AbstractKtInventory) return@forEach
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
