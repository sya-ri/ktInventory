package dev.s7a.ktinventory

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack

abstract class KtInventoryBase(
    protected val line: Int,
) {
    init {
        require(line in 1..6) { "line must be between 1 and 6" }
    }

    private val _buttons = mutableMapOf<Int, KtInventoryButton<KtInventoryBase>>()

    val buttons
        get() = _buttons.toMap()

    val size: Int
        get() = line * 9

    fun button(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<KtInventory>) -> Unit = {},
    ) {
        button(slot, KtInventoryButton(itemStack, onClick))
    }

    open fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        require(slot in 0 until size) { "slot must be between 0 and $size" }
        _buttons[slot] = item
    }

    open fun onOpen(event: InventoryOpenEvent) {}

    fun onClick(event: InventoryClickEvent) {
        val slot = event.slot
        val button = buttons[slot]
        if (button != null) {
            val player = event.whoClicked
            val click = event.click
            val cursor = event.cursor
            button.onClick(KtInventoryButton.ClickEvent(this, player, click, cursor))
        }
    }

    open fun onClose(event: InventoryCloseEvent) {}

    abstract fun open(player: HumanEntity)
}
