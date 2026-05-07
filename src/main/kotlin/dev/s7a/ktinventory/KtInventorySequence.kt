package dev.s7a.ktinventory

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import kotlin.reflect.KClass

/**
 * Abstract class for sequence-backed inventories with customizable titles.
 *
 * @param context Plugin context
 * @param line Number of inventory lines (1-6)
 * @param altColorChar The alternate color code character for title color formatting, defaults to '&'
 * @since 2.2.0
 */
abstract class KtInventorySequence(
    private val context: KtInventoryPluginContext,
    line: Int,
    private val altColorChar: Char? = '&',
) : AbstractKtInventorySequence<KtInventorySequence>(context, line) {
    /**
     * Generates the title for a specific page of the inventory.
     *
     * @param page Current page number
     * @return The formatted title string
     * @since 2.2.0
     */
    abstract fun title(page: Int): String

    final override fun createEntry(page: Int): Entry<KtInventorySequence> = Entry(this, page)

    /**
     * Represents a single page entry in the sequence-backed inventory.
     *
     * @param T Type of the sequence-backed inventory
     * @param paginated The sequence-backed inventory instance
     * @param page Current page number
     * @since 2.2.0
     */
    class Entry<T : KtInventorySequence>(
        paginated: T,
        page: Int,
    ) : AbstractKtInventorySequence.Entry<T>(paginated, page) {
        @Suppress("DEPRECATION")
        private val _inventory by lazy {
            @Suppress("DEPRECATION")
            Bukkit.createInventory(
                this,
                size,
                if (paginated.altColorChar != null) {
                    ChatColor.translateAlternateColorCodes(paginated.altColorChar, paginated.title(page))
                } else {
                    paginated.title(page)
                },
            )
        }

        override fun getInventory() = _inventory
    }

    /**
     * Abstract class for refreshable sequence-backed inventories.
     *
     * @param T Type of the sequence-backed inventory
     * @param clazz The KClass of the sequence-backed inventory type
     * @since 2.2.0
     */
    abstract class Refreshable<T : KtInventorySequence>(
        clazz: KClass<T>,
    ) : AbstractKtInventorySequence.Refreshable<T>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventorySequence.Entry<T>,
        ): T?
    }
}
