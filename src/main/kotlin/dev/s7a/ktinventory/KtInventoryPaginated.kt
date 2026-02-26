package dev.s7a.ktinventory

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

/**
 * Abstract class for paginated inventories with customizable titles.
 *
 * @param context Plugin context
 * @param line Number of inventory lines (1-6)
 * @param altColorChar The alternate color code character for title color formatting, defaults to '&'
 * @since 2.1.0
 */
abstract class KtInventoryPaginated(
    private val context: KtInventoryPluginContext,
    line: Int,
    private val altColorChar: Char? = '&',
) : AbstractKtInventoryPaginated<KtInventoryPaginated>(context, line) {
    /**
     * @param plugin The plugin instance
     * @param line Number of inventory lines (1-6)
     * @param altColorChar The alternate color code character for title color formatting, defaults to '&'
     * @since 2.0.0
     */
    @Deprecated("Use KtInventoryPluginContext constructor instead")
    constructor(plugin: Plugin, line: Int, altColorChar: Char? = '&') : this(
        KtInventoryPluginContext(plugin),
        line,
        altColorChar,
    )

    /**
     * Generates the title for a specific page of the inventory.
     *
     * @param page Current page number
     * @param lastPage Last page number
     * @return The formatted title string
     * @since 2.0.0
     */
    abstract fun title(
        page: Int,
        lastPage: Int,
    ): String

    final override fun createEntry(
        page: Int,
        lastPage: Int,
    ): Entry<KtInventoryPaginated> = Entry(this, page, lastPage)

    /**
     * Represents a single page entry in the paginated inventory.
     *
     * @param T Type of the paginated inventory
     * @param paginated The paginated inventory instance
     * @param page Current page number
     * @param lastPage Last page number
     * @since 2.0.0
     */
    class Entry<T : KtInventoryPaginated>(
        paginated: T,
        page: Int,
        lastPage: Int,
    ) : AbstractKtInventoryPaginated.Entry<T>(paginated, page, lastPage) {
        @Suppress("DEPRECATION")
        private val _inventory by lazy {
            @Suppress("DEPRECATION")
            Bukkit.createInventory(
                this,
                size,
                if (paginated.altColorChar != null) {
                    ChatColor.translateAlternateColorCodes(paginated.altColorChar, paginated.title(page, lastPage))
                } else {
                    paginated.title(page, lastPage)
                },
            )
        }

        override fun getInventory() = _inventory
    }

    /**
     * Abstract class for refreshable paginated inventories.
     *
     * @param T Type of the paginated inventory
     * @param clazz The KClass of the paginated inventory type
     * @since 2.0.0
     */
    abstract class Refreshable<T : KtInventoryPaginated>(
        clazz: KClass<T>,
    ) : AbstractKtInventoryPaginated.Refreshable<T>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventoryPaginated.Entry<T>,
        ): T?
    }
}
