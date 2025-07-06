package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

abstract class KtInventoryAdventure(
    plugin: Plugin,
    line: Int,
) : AbstractKtInventory(plugin, line) {
    abstract fun title(): Component

    private val _inventory by lazy {
        plugin.server.createInventory(this, size, title())
    }

    override fun getInventory() = _inventory
}
