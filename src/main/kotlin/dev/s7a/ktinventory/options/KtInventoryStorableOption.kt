package dev.s7a.ktinventory.options

import org.bukkit.event.inventory.InventoryCloseEvent

interface KtInventoryStorableOption {
    fun allowSave(event: InventoryCloseEvent): Boolean

    data object Default : KtInventoryStorableOption {
        override fun allowSave(event: InventoryCloseEvent) = event.viewers.size == 1
    }
}
