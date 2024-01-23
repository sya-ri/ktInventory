package dev.s7a.ktinventory

import dev.s7a.ktinventory.internal.color
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

/**
 * [KtInventory] creator
 *
 * @property plugin that handles events.
 * @since 1.0.0
 */
class KtInventoryProvider(private val plugin: Plugin) {
    /**
     * Create [KtInventory] from [Inventory].
     *
     * @param bukkitInventory [Inventory] used to generate [KtInventory].
     * @since 1.0.0
     */
    fun get(bukkitInventory: Inventory): KtInventory {
        return plugin.ktInventory(bukkitInventory)
    }

    /**
     * Create [KtInventory] from [Inventory].
     *
     * @param bukkitInventory [Inventory] used to generate [KtInventory].
     * @param block Processing using [KtInventory].
     * @since 1.0.0
     */
    inline fun get(bukkitInventory: Inventory, block: KtInventory.() -> Unit): KtInventory {
        return get(bukkitInventory).apply(block)
    }

    /**
     * Create [KtInventory] of chest type [Inventory].
     *
     * @param title Inventory title.
     * @param line Inventory size (must be 1-6).
     * @param altColorChar Alternate color code character to use for [title]. If null, do nothing. (default: '&')
     * @since 1.0.0
     */
    fun get(title: String, line: Int = 3, altColorChar: Char? = '&'): KtInventory {
        require(line in 1..6)
        return get(Bukkit.createInventory(null, line * 9, title.color(altColorChar)))
    }

    /**
     * Create [KtInventory] of chest type [Inventory].
     *
     * @param title Inventory title.
     * @param line Inventory size (must be 1-6).
     * @param altColorChar Alternate color code character to use for [title]. If null, do nothing. (default: '&')
     * @param block Processing using [KtInventory].
     * @since 1.0.0
     */
    inline fun get(title: String, line: Int = 3, altColorChar: Char? = '&', block: KtInventory.() -> Unit): KtInventory {
        return get(title, line, altColorChar).apply(block)
    }

    /**
     * Create [KtInventory] of specific type [Inventory].
     *
     * @param title Inventory title.
     * @param type Inventory type.
     * @param altColorChar Alternate color code character to use for [title]. If null, do nothing. (default: '&')
     * @since 1.0.0
     */
    fun get(title: String, type: InventoryType, altColorChar: Char? = '&'): KtInventory {
        return get(Bukkit.createInventory(null, type, title.color(altColorChar)))
    }

    /**
     * Create [KtInventory] of specific type [Inventory].
     *
     * @param title Inventory title.
     * @param type Inventory type.
     * @param altColorChar Alternate color code character to use for [title]. If null, do nothing. (default: '&')
     * @param block Processing using [KtInventory].
     * @since 1.0.0
     */
    inline fun get(title: String, type: InventoryType, altColorChar: Char? = '&', block: KtInventory.() -> Unit): KtInventory {
        return get(title, type, altColorChar).apply(block)
    }
}
