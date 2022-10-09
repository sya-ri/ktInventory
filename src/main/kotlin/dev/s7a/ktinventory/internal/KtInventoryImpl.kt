package dev.s7a.ktinventory.internal

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryContent
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

internal class KtInventoryImpl(private val handler: KtInventoryHandler, override val bukkitInventory: Inventory) : KtInventory {
    var onClick: ((InventoryClickEvent) -> Unit)? = { it.isCancelled = true }
    var onClose: ((InventoryCloseEvent) -> Unit)? = null
    val actions = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    override fun item(index: Int, itemStack: ItemStack): KtInventoryContent {
        if (index in 0 until bukkitInventory.size) {
            return KtInventoryContentImpl(this, index).apply {
                this.itemStack = itemStack
            }
        } else {
            throw IndexOutOfBoundsException("index must be in the range of bukkitInventory")
        }
    }

    override fun item(index: Int, itemStack: ItemStack, block: ((InventoryClickEvent) -> Unit)?): KtInventoryContent {
        return item(index, itemStack).apply {
            if (block != null) {
                actions[index] = block
            } else {
                actions.remove(index)
            }
        }
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
