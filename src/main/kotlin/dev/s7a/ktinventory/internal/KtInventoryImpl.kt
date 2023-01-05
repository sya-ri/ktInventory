package dev.s7a.ktinventory.internal

import dev.s7a.ktinventory.KtInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

internal class KtInventoryImpl(private val handler: KtInventoryHandler, override val bukkitInventory: Inventory) : KtInventory {
    var onOpen: ((InventoryOpenEvent) -> Unit)? = null
    var onClick: ((InventoryClickEvent) -> Unit)? = { it.isCancelled = true }
    var onClose: ((InventoryCloseEvent) -> Unit)? = null
    val actions = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    override fun item(index: Int, itemStack: ItemStack) {
        if (index in 0 until bukkitInventory.size) {
            bukkitInventory.setItem(index, itemStack)
        } else {
            throw IndexOutOfBoundsException("index must be in the range of bukkitInventory")
        }
    }

    override fun item(index: Int, itemStack: ItemStack, block: ((InventoryClickEvent) -> Unit)?) {
        item(index, itemStack)
        if (block != null) {
            actions[index] = block
        } else {
            actions.remove(index)
        }
    }

    override fun onOpen(block: ((InventoryOpenEvent) -> Unit)?) {
        onOpen = block
    }

    override fun onClick(block: ((InventoryClickEvent) -> Unit)?) {
        onClick = block
    }

    override fun onClose(block: ((InventoryCloseEvent) -> Unit)?) {
        onClose = block
    }

    override fun open(player: HumanEntity) {
        handler.open(player, this)
    }
}
