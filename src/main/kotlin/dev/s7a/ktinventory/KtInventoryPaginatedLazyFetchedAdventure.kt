package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import kotlin.reflect.KClass

/**
 * Abstract class for lazy fetched inventories with Adventure Component titles.
 *
 * @param C Type of the display condition
 * @param D Type of the fetched data
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class KtInventoryPaginatedLazyFetchedAdventure<C : Any, D>(
    context: KtInventoryPluginContext.LazyFetchable,
    line: Int,
) : AbstractKtInventoryPaginatedLazyFetched<KtInventoryPaginatedLazyFetchedAdventure<C, D>, C, D>(context, line) {
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
        paginated: KtInventoryPaginatedLazyFetchedAdventure<C, D>,
        condition: C,
    ) : AbstractKtInventoryPaginatedLazyFetched.Entry<KtInventoryPaginatedLazyFetchedAdventure<C, D>, C, D>(paginated, condition) {
        private val _inventory by lazy {
            Bukkit.createInventory(this, size, paginated.title(condition))
        }

        override fun getInventory() = _inventory
    }

    /**
     * Abstract class for refreshable lazy-fetched Adventure inventories.
     *
     * @param T Type of the lazy-fetched inventory
     * @param C Type of the display condition
     * @param clazz The KClass of the lazy-fetched inventory type
     * @since 2.2.0
     */
    abstract class Refreshable<T : KtInventoryPaginatedLazyFetchedAdventure<C, *>, C : Any>(
        clazz: KClass<T>,
    ) : AbstractKtInventoryPaginatedLazyFetched.Refreshable<T, C>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventoryPaginatedLazyFetched.Entry<*, C, *>,
        ): T?
    }
}
