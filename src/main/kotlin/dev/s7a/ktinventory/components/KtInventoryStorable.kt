package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.AbstractKtInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType
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

    data class ClickEvent(
        val player: HumanEntity,
        val click: ClickType,
        val cursor: ItemStack?,
        val slot: Int,
    )

    data class DragEvent(
        val player: HumanEntity,
        val cursor: ItemStack?,
        val oldCursor: ItemStack,
        val slots: Set<Int>,
    )

    enum class EventResult {
        Allow,
        Deny,
    }
}
