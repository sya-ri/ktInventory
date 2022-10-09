package dev.s7a.ktinventory

import org.bukkit.inventory.ItemStack

/**
 * KtInventory slot contents
 *
 * @since 1.0.0
 */
interface KtInventoryContent {
    /**
     * Item placed in the slot
     *
     * @since 1.0.0
     */
    var itemStack: ItemStack?
}
