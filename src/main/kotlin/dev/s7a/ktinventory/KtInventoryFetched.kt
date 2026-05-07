package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit

/**
 * Abstract class for fetched inventories with customizable titles.
 *
 * @param C Type of the display condition
 * @param context Plugin context
 * @param line Number of inventory rows
 * @param altColorChar The alternate color code character for title color formatting, defaults to '&'
 * @since 2.2.0
 */
abstract class KtInventoryFetched<C : Any>(
    private val context: KtInventoryPluginContext,
    line: Int,
    private val altColorChar: Char? = '&',
) : AbstractKtInventoryFetched<KtInventoryFetched<C>, C>(context, line) {
    /**
     * Generates the title for a specific display condition.
     *
     * @param condition Current display condition
     * @return The formatted title string
     * @since 2.2.0
     */
    abstract fun title(condition: C): String

    final override fun createEntry(
        condition: C,
        page: Page<C, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetched<C>, C>>>,
    ): Entry<C> = Entry(this, condition, page)

    /**
     * Represents a single page entry in the fetched inventory.
     *
     * @param C Type of the display condition
     * @param paginated The fetched inventory instance
     * @param condition Current display condition
     * @param page Fetched page data
     * @since 2.2.0
     */
    class Entry<C : Any>(
        paginated: KtInventoryFetched<C>,
        condition: C,
        page: Page<C, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetched<C>, C>>>,
    ) : AbstractKtInventoryFetched.Entry<KtInventoryFetched<C>, C>(paginated, condition, page) {
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
