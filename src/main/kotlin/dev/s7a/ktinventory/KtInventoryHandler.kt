package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.util.getTopInventory
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.server.PluginDisableEvent
import java.util.IdentityHashMap

/**
 * Internal handler for KtInventory events.
 *
 * @property context Plugin context
 * @since 2.0.0
 */
internal class KtInventoryHandler(
    private val context: KtInventoryPluginContext,
) : Listener {
    @EventHandler
    fun on(event: InventoryOpenEvent) {
        val inventory = event.inventory.holder as? AbstractKtInventory ?: return
        if (inventory.handlerId !== context.handlerId) return
        inventory.onOpen(event)
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val inventory = event.inventory.holder as? AbstractKtInventory ?: return
        if (inventory.handlerId !== context.handlerId) return

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
        if (inventory.handlerId !== context.handlerId) return

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
        if (inventory.handlerId !== context.handlerId) return

        // Storable Save
        if (inventory.storableOption.allowSave(event)) {
            inventory.saveStorables()
        }

        // Inventory OnClose
        inventory.onClose(event)
    }

    @EventHandler
    fun on(event: PluginDisableEvent) {
        val disabledHandlerKey = KtInventoryHandlerId.find(event.plugin) ?: return
        if (context.handlerId === disabledHandlerKey) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (getTopInventory<AbstractKtInventory>(player)?.handlerId === disabledHandlerKey) {
                    player.closeInventory()
                }
            }
            handlers.remove(context.handlerId)
            KtInventoryHandlerId.remove(event.plugin)
        }
    }

    companion object {
        private val handlers = IdentityHashMap<KtInventoryHandlerId, KtInventoryHandler>()

        @Synchronized
        fun register(context: KtInventoryPluginContext): KtInventoryHandler? =
            handlers.getOrPut(context.handlerId) {
                KtInventoryHandler(context).apply {
                    context.registerEvents(this)
                }
            }
    }
}
