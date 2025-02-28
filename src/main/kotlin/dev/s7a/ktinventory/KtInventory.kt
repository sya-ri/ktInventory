package dev.s7a.ktinventory

import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

abstract class KtInventory(
    protected val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line),
    InventoryHolder {
    abstract fun title(): String

    private val bukkitInventory by lazy {
        plugin.server.createInventory(this, line * 9, ChatColor.translateAlternateColorCodes('&', title()))
    }

    fun createButton(
        itemStack: ItemStack,
        onClick: (InventoryClickEvent, KtInventory) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        super.button(slot, item)
        bukkitInventory.setItem(slot, item.itemStack)
    }

    final override fun open(player: HumanEntity) {
        KtInventoryHandler.register(plugin)
        player.openInventory(bukkitInventory)
    }

    final override fun getInventory() = bukkitInventory
}
