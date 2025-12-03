package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

/**
 * An abstract inventory implementation that uses Adventure's Component for titles.
 *
 * @param plugin The plugin instance
 * @param line Number of inventory rows
 * @since 2.0.0
 */
abstract class KtInventoryAdventure(
    plugin: Plugin,
    line: Int,
) : AbstractKtInventory(plugin, line) {
    /**
     * Returns the title of this inventory as a Component.
     *
     * @return The inventory title
     * @since 2.0.0
     */
    abstract fun title(): Component

    private val _inventory by lazy {
        plugin.server.createInventory(this, size, title())
    }

    override fun getInventory() = _inventory
}
