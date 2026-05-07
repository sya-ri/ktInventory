package dev.s7a.ktinventory

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit

/**
 * Abstract class for lazy fetched inventories with customizable titles.
 *
 * @param C Type of the display condition
 * @param D Type of the fetched data
 * @param context Plugin context
 * @param line Number of inventory rows
 * @param altColorChar The alternate color code character for title color formatting, defaults to '&'
 * @since 2.2.0
 */
abstract class KtInventoryLazyFetched<C : Any, D>(
    private val context: KtInventoryPluginContext.LazyFetchable,
    line: Int,
    private val altColorChar: Char? = '&',
) : AbstractKtInventoryLazyFetched<KtInventoryLazyFetched<C, D>, C, D>(context, line) {
    /**
     * Generates the title for a specific display condition.
     *
     * @param condition Current display condition
     * @return The formatted title string
     * @since 2.2.0
     */
    abstract fun title(condition: C): String

    final override fun createEntry(condition: C): Entry<C, D> = Entry(this, condition)

    /**
     * Represents a single page entry in the lazy fetched inventory.
     *
     * @param C Type of the display condition
     * @param D Type of the fetched data
     * @param paginated The lazy fetched inventory instance
     * @param condition Current display condition
     * @since 2.2.0
     */
    class Entry<C : Any, D>(
        paginated: KtInventoryLazyFetched<C, D>,
        condition: C,
    ) : AbstractKtInventoryLazyFetched.Entry<KtInventoryLazyFetched<C, D>, C, D>(paginated, condition) {
        @Suppress("DEPRECATION")
        private val _inventory by lazy {
            @Suppress("DEPRECATION")
            Bukkit.createInventory(
                this,
                size,
                if (paginated.altColorChar != null) {
                    ChatColor.translateAlternateColorCodes(paginated.altColorChar, paginated.title(condition))
                } else {
                    paginated.title(condition)
                },
            )
        }

        override fun getInventory() = _inventory
    }
}
