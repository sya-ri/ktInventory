package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

/**
 * Abstract class for fetched inventories with Adventure Component titles.
 *
 * @param C Type of the display condition
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class KtInventoryFetchedAdventure<C : Any>(
    context: KtInventoryPluginContext,
    line: Int,
) : AbstractKtInventoryFetched<KtInventoryFetchedAdventure<C>, C>(context, line) {
    /**
     * Generates the title for a specific display condition.
     *
     * @param condition Current display condition
     * @return The formatted title component
     * @since 2.2.0
     */
    abstract fun title(condition: C): Component

    final override fun createEntry(
        condition: C,
        page: Page<C, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetchedAdventure<C>, C>>>,
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
        paginated: KtInventoryFetchedAdventure<C>,
        condition: C,
        page: Page<C, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetchedAdventure<C>, C>>>,
    ) : AbstractKtInventoryFetched.Entry<KtInventoryFetchedAdventure<C>, C>(paginated, condition, page) {
        private val _inventory by lazy {
            Bukkit.createInventory(this, size, paginated.title(condition))
        }

        override fun getInventory() = _inventory
    }
}
