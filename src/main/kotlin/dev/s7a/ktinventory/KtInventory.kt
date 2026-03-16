package dev.s7a.ktinventory

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/**
 * Abstract inventory class that creates an inventory with a customizable title.
 * Extends [AbstractKtInventory] to provide basic inventory functionality.
 *
 * @param context Plugin context
 * @param line Number of inventory rows
 * @param altColorChar Character used for color codes in the title. Default is '&'. Set to null to disable color code translation.
 * @since 2.1.0
 */
abstract class KtInventory(
    context: KtInventoryPluginContext,
    line: Int,
    altColorChar: Char? = '&',
) : AbstractKtInventory(context, line) {
    /**
     * @param plugin The plugin instance
     * @param line Number of inventory rows
     * @param altColorChar Character used for color codes in the title. Default is '&'. Set to null to disable color code translation.
     * @since 2.0.0
     */
    @Deprecated("Use KtInventoryPluginContext constructor instead")
    constructor(plugin: Plugin, line: Int, altColorChar: Char? = '&') : this(
        KtInventoryPluginContext(plugin),
        line,
        altColorChar,
    )

    /**
     * Gets the title of the inventory.
     * The title can include color codes if [altColorChar] is not null.
     *
     * @return The inventory title.
     * @since 2.0.0
     */
    abstract fun title(): String

    @Suppress("DEPRECATION")
    private val _inventory by lazy {
        @Suppress("DEPRECATION")
        Bukkit.createInventory(
            this,
            size,
            if (altColorChar != null) ChatColor.translateAlternateColorCodes(altColorChar, title()) else title(),
        )
    }

    override fun getInventory() = _inventory
}
