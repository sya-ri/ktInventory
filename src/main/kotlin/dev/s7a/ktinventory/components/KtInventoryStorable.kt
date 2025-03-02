package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.KtInventory
import org.bukkit.inventory.ItemStack

class KtInventoryStorable internal constructor(
    val inventory: KtInventory,
    val slots: List<Int>,
    var canEdit: Boolean,
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
}
