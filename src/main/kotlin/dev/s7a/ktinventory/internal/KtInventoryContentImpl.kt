package dev.s7a.ktinventory.internal

import dev.s7a.ktinventory.KtInventoryContent
import org.bukkit.inventory.ItemStack

internal data class KtInventoryContentImpl(private val inventory: KtInventoryImpl, private val index: Int) : KtInventoryContent {
    override var itemStack: ItemStack?
        get() = inventory.bukkitInventory.getItem(index)
        set(value) {
            inventory.bukkitInventory.setItem(index, value)
        }
}
