package dev.s7a.ktinventory

import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.InventoryHolder
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
