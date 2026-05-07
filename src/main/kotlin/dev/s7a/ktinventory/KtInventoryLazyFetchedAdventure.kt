package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

/**
 * Abstract class for lazy fetched inventories with Adventure Component titles.
 *
 * @param C Type of the display condition
 * @param D Type of the fetched data
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class KtInventoryLazyFetchedAdventure<C : Any, D>(
    context: KtInventoryPluginContext.LazyFetchable,
    line: Int,
) : AbstractKtInventoryLazyFetched<KtInventoryLazyFetchedAdventure<C, D>, C, D>(context, line) {
    /**
     * Generates the title for a specific display condition.
     *
     * @param condition Current display condition
     * @return The formatted title component
     * @since 2.2.0
     */
    abstract fun title(condition: C): Component

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
        paginated: KtInventoryLazyFetchedAdventure<C, D>,
        condition: C,
    ) : AbstractKtInventoryLazyFetched.Entry<KtInventoryLazyFetchedAdventure<C, D>, C, D>(paginated, condition) {
        private val _inventory by lazy {
            Bukkit.createInventory(this, size, paginated.title(condition))
        }

        override fun getInventory() = _inventory
    }
}
