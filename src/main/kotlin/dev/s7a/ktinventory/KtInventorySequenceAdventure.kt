package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import kotlin.reflect.KClass

/**
 * Abstract class for sequence-backed inventories with Adventure Component titles.
 *
 * @param context Plugin context
 * @param line Number of inventory lines (1-6)
 * @since 2.2.0
 */
abstract class KtInventorySequenceAdventure(
    context: KtInventoryPluginContext,
    line: Int,
) : AbstractKtInventorySequence<KtInventorySequenceAdventure>(context, line) {
    /**
     * Generates the Component title for a specific page of the inventory.
     *
     * @param page Current page number
     * @return The formatted Component title
     * @since 2.2.0
     */
    abstract fun title(page: Int): Component

    final override fun createEntry(page: Int): Entry<KtInventorySequenceAdventure> = Entry(this, page)

    /**
     * Represents a single page entry in the sequence-backed inventory.
     *
     * @param T Type of the sequence-backed inventory
     * @param paginated The sequence-backed inventory instance
     * @param page Current page number
     * @since 2.2.0
     */
    class Entry<T : KtInventorySequenceAdventure>(
        paginated: T,
        page: Int,
    ) : AbstractKtInventorySequence.Entry<T>(paginated, page) {
        private val _inventory by lazy {
            Bukkit.createInventory(this, size, paginated.title(page))
        }

        override fun getInventory() = _inventory
    }

    /**
     * Abstract class for refreshable sequence-backed inventories with Adventure Component titles.
     *
     * @param T Type of the sequence-backed inventory
     * @param clazz The KClass of the sequence-backed inventory type
     * @since 2.2.0
     */
    abstract class Refreshable<T : KtInventorySequenceAdventure>(
        clazz: KClass<T>,
    ) : AbstractKtInventorySequence.Refreshable<T>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventorySequence.Entry<T>,
        ): T?
    }
}
