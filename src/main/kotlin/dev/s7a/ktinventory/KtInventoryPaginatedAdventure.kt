package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

/**
 * Abstract class for paginated inventories with Adventure Component titles.
 *
 * @param plugin The plugin instance
 * @param line Number of inventory lines (1-6)
 * @since 2.0.0
 */
abstract class KtInventoryPaginatedAdventure(
    private val plugin: Plugin,
    line: Int,
) : AbstractKtInventoryPaginated<KtInventoryPaginatedAdventure>(plugin, line) {
    /**
     * Generates the Component title for a specific page of the inventory.
     *
     * @param page Current page number
     * @param lastPage Last page number
     * @return The formatted Component title
     * @since 2.0.0
     */
    abstract fun title(
        page: Int,
        lastPage: Int,
    ): Component

    final override fun createEntry(
        page: Int,
        lastPage: Int,
    ): Entry<KtInventoryPaginatedAdventure> = Entry(this, page, lastPage)

    /**
     * Represents a single page entry in the paginated inventory.
     *
     * @param T Type of the paginated inventory
     * @param paginated The paginated inventory instance
     * @param page Current page number
     * @param lastPage Last page number
     * @since 2.0.0
     */
    class Entry<T : KtInventoryPaginatedAdventure>(
        paginated: T,
        page: Int,
        lastPage: Int,
    ) : AbstractKtInventoryPaginated.Entry<T>(paginated, page, lastPage) {
        private val _inventory by lazy {
            paginated.plugin.server.createInventory(this, size, paginated.title(page, lastPage))
        }

        override fun getInventory() = _inventory
    }

    /**
     * Abstract class for refreshable paginated inventories with Adventure Component titles.
     *
     * @param T Type of the paginated inventory
     * @param clazz The KClass of the paginated inventory type
     * @since 2.0.0
     */
    abstract class Refreshable<T : KtInventoryPaginatedAdventure>(
        clazz: KClass<T>,
    ) : AbstractKtInventoryPaginated.Refreshable<T>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventoryPaginated.Entry<T>,
        ): T?
    }
}
