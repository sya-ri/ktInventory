package dev.s7a.ktinventory

import net.md_5.bungee.api.ChatColor
import org.bukkit.plugin.Plugin

abstract class KtInventory(
    plugin: Plugin,
    line: Int,
    altColorChar: Char? = '&',
) : AbstractKtInventory(plugin, line) {
    abstract fun title(): String

    private val _inventory by lazy {
        plugin.server.createInventory(
            this,
            size,
            if (altColorChar != null) ChatColor.translateAlternateColorCodes(altColorChar, title()) else title(),
        )
    }

    override fun getInventory() = _inventory
}
