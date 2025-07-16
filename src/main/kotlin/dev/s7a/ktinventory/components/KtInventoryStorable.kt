package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.AbstractKtInventory
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

class KtInventoryStorable internal constructor(
    val inventory: AbstractKtInventory,
    val slots: List<Int>,
    val onPreClick: (ClickEvent) -> EventResult,
    val onClick: (ClickEvent) -> Unit,
    val onPreDrag: (DragEvent) -> EventResult,
    val onDrag: (DragEvent) -> Unit,
    private val save: (List<ItemStack?>) -> Unit,
) {
    fun contains(slot: Int) = slots.contains(slot)

    fun update(items: List<ItemStack?>): List<ItemStack?> {
        slots.forEachIndexed { index, slot ->
            val item = items.getOrNull(index)
            inventory.setItem(slot, item)
        }
        return items.drop(slots.size)
    }

    fun clear() {
        update(emptyList())
    }

    fun get() = slots.map(inventory::getItem)

    fun save() {
        save(get())
    }

    class ClickEvent(
        private val event: InventoryClickEvent,
    ) {
        @Deprecated("Use wrapper")
        fun unsafe() = event

        val player
            get() = event.whoClicked

        val click
            get() = event.click

        val hotbarButton
            get() = event.hotbarButton

        val slot
            get() = event.slot
    }

    class DragEvent(
        private val event: InventoryDragEvent,
    ) {
        @Deprecated("Use wrapper")
        fun unsafe() = event

        val player
            get() = event.whoClicked

        val slots
            get() = event.rawSlots

        val newItems
            get() = event.newItems

        val cursor
            get() = event.cursor

        val oldCursor
            get() = event.oldCursor
    }

    enum class EventResult {
        Allow,
        Deny,
    }
}
