package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import kotlin.reflect.KClass

/**
 * Abstract class for fetched inventories with Adventure Component titles.
 *
 * @param C Type of the display condition
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class KtInventoryPaginatedFetchedAdventure<C : Any>(
    context: KtInventoryPluginContext,
    line: Int,
) : AbstractKtInventoryPaginatedFetched<KtInventoryPaginatedFetchedAdventure<C>, C>(context, line) {
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
        page: Page<C, KtInventoryButton<AbstractKtInventoryPaginatedFetched.Entry<KtInventoryPaginatedFetchedAdventure<C>, C>>>,
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
        paginated: KtInventoryPaginatedFetchedAdventure<C>,
        condition: C,
        page: Page<C, KtInventoryButton<AbstractKtInventoryPaginatedFetched.Entry<KtInventoryPaginatedFetchedAdventure<C>, C>>>,
    ) : AbstractKtInventoryPaginatedFetched.Entry<KtInventoryPaginatedFetchedAdventure<C>, C>(paginated, condition, page) {
        private val _inventory by lazy {
            Bukkit.createInventory(this, size, paginated.title(condition))
        }

        override fun getInventory() = _inventory
    }

    /**
     * Abstract class for refreshable condition-fetched Adventure inventories.
     *
     * @param T Type of the condition-fetched inventory
     * @param C Type of the display condition
     * @param clazz The KClass of the condition-fetched inventory type
     * @since 2.2.0
     */
    abstract class Refreshable<T : KtInventoryPaginatedFetchedAdventure<C>, C : Any>(
        clazz: KClass<T>,
    ) : AbstractKtInventoryPaginatedFetched.Refreshable<T, C>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventoryPaginatedFetched.Entry<*, C>,
        ): T?
    }
}
