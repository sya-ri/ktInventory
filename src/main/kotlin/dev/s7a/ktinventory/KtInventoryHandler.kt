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

/**
 * Internal handler for KtInventory events.
 *
 * @property plugin The plugin instance
 * @since 2.0.0
 */
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
            if (storables.isEmpty() || storables.map { it.onPreClick(storableClickEvent) }.contains(KtInventoryStorable.EventResult.Deny)) {
                event.isCancelled = true
            } else {
                // Storable OnClick
                storables.forEach { storable ->
                    storable.onClick(storableClickEvent)
                }
            }

            // Button
            inventory.handleClick(event)

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
        val storables = inventory.getStorables(event.inventorySlots)
        val storableDragEvent = KtInventoryStorable.DragEvent(event)
        if (storables.isEmpty() || storables.map { it.onPreDrag(storableDragEvent) }.contains(KtInventoryStorable.EventResult.Deny)) {
            event.isCancelled = true
        } else {
            // Storable Drag
            storables.forEach { storable ->
                storable.onDrag(storableDragEvent)
            }
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
