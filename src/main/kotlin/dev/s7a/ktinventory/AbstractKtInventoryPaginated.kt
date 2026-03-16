package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.util.getAllViewersPaginated
import dev.s7a.ktinventory.util.getOpenInventoryPaginated
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Base class for paginated inventories
 *
 * @param T Type of the paginated inventory
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.1.0
 */
abstract class AbstractKtInventoryPaginated<T : AbstractKtInventoryPaginated<T>>(
    private val context: KtInventoryPluginContext,
    line: Int,
) : KtInventoryBase(line) {
    /**
     * @param plugin Plugin instance
     * @param line Number of inventory rows
     * @since 2.0.0
     */
    @Deprecated("Use KtInventoryPluginContext constructor instead")
    constructor(plugin: Plugin, line: Int) : this(KtInventoryPluginContext(plugin), line)

    /**
     * List of slot numbers used for pagination
     *
     * @since 2.0.0
     */
    private val paginates = mutableListOf<Int>()

    /**
     * List of entries to be paginated
     *
     * @since 2.0.0
     */
    abstract val entries: List<KtInventoryButton<Entry<T>>>

    /**
     * Cache of created inventory pages
     *
     * @since 2.0.0
     */
    private val pages = ConcurrentHashMap<Int, Entry<T>>()

    /**
     * Creates a new inventory page
     *
     * @param page Page number
     * @param lastPage Last page number
     * @return Created inventory page
     * @since 2.0.0
     */
    protected abstract fun createEntry(
        page: Int,
        lastPage: Int,
    ): Entry<T>

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        super.button(slot, item)
        pages.values.forEach { page ->
            page.button(slot, item)
        }
    }

    /**
     * Sets slots to be used for pagination
     *
     * @param slots Slot numbers
     * @since 2.0.0
     */
    fun paginateSlot(vararg slots: Int) {
        paginateSlot(slots.toList())
    }

    /**
     * Sets slots to be used for pagination
     *
     * @param slots Slot numbers
     * @since 2.0.0
     */
    fun paginateSlot(slots: Iterable<Int>) {
        this.paginates.addAll(slots)
    }

    /**
     * Creates a new inventory button
     *
     * @param itemStack Item to display
     * @param onClick Click handler
     * @return Created button
     * @since 2.0.0
     */
    fun createButton(
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T>>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    /**
     * Adds a button to navigate to the next page
     *
     * @param slot Button slot number
     * @param itemStack Item to display
     * @param onClick Additional click handler
     * @since 2.0.0
     */
    fun nextPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T>>) -> Unit = {},
    ) {
        nextPageButton(slot, createButton(itemStack, onClick))
    }

    /**
     * Adds a button to navigate to the next page
     *
     * @param slot Button slot number
     * @param item Button to use
     * @since 2.0.0
     */
    fun nextPageButton(
        slot: Int,
        item: KtInventoryButton<Entry<T>>,
    ) {
        button(
            slot,
            item.join { event ->
                event.inventory.openNextPage(event.player)
            },
        )
    }

    /**
     * Adds a button to navigate to the previous page
     *
     * @param slot Button slot number
     * @param itemStack Item to display
     * @param onClick Additional click handler
     * @since 2.0.0
     */
    fun previousPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T>>) -> Unit = {},
    ) {
        previousPageButton(slot, createButton(itemStack, onClick))
    }

    /**
     * Adds a button to navigate to the previous page
     *
     * @param slot Button slot number
     * @param item Button to use
     * @since 2.0.0
     */
    fun previousPageButton(
        slot: Int,
        item: KtInventoryButton<Entry<T>>,
    ) {
        button(
            slot,
            item.join { event ->
                event.inventory.openPreviousPage(event.player)
            },
        )
    }

    final override fun open(player: HumanEntity) {
        open(player, 0)
    }

    /**
     * Opens specific page of the inventory for a player
     *
     * @param player Player to open inventory for
     * @param page Page number to open
     * @since 2.0.0
     */
    fun open(
        player: HumanEntity,
        page: Int,
    ) {
        val chunked = entries.chunked(this.paginates.size)
        val lastPage = chunked.lastIndex
        when {
            page < 0 -> {
                open(player, 0)
            }

            page != 0 && lastPage < page -> {
                open(player, lastPage)
            }

            else -> {
                pages
                    .computeIfAbsent(page) {
                        val slots = this.paginates
                        val buttons = this.buttons
                        createEntry(page, lastPage)
                            .apply {
                                val chunk = chunked.getOrNull(page).orEmpty()
                                slots.forEachIndexed { index, slot ->
                                    chunk.getOrNull(index)?.let {
                                        button(slot, it)
                                    }
                                }
                                buttons.forEach { (slot, item) ->
                                    button(slot, item)
                                }
                            }
                    }.open(player)
            }
        }
    }

    /**
     * Represents a single page of paginated inventory
     *
     * @param T Type of the paginated inventory
     * @param paginated Parent paginated inventory
     * @param page Current page number
     * @param lastPage Last page number
     * @since 2.0.0
     */
    abstract class Entry<T : AbstractKtInventoryPaginated<*>>(
        val paginated: T,
        val page: Int,
        val lastPage: Int,
    ) : AbstractKtInventory(paginated.context, paginated.line) {
        /**
         * Opens next page for a player
         *
         * @param player Player to open inventory for
         * @since 2.0.0
         */
        fun openNextPage(player: HumanEntity) {
            paginated.open(player, page + 1)
        }

        /**
         * Opens previous page for a player
         *
         * @param player Player to open inventory for
         * @since 2.0.0
         */
        fun openPreviousPage(player: HumanEntity) {
            paginated.open(player, page - 1)
        }

        override fun onOpen(event: InventoryOpenEvent) = paginated.onOpen(event)

        override fun onClick(event: InventoryClickEvent) = paginated.onClick(event)

        override fun onClose(event: InventoryCloseEvent) = paginated.onClose(event)
    }

    /**
     * Base class for refreshable paginated inventories
     *
     * @param T Type of the paginated inventory
     * @param clazz Class of the paginated inventory
     * @since 2.0.0
     */
    abstract class Refreshable<T : AbstractKtInventoryPaginated<*>>(
        val clazz: KClass<T>,
    ) : RefreshableInventory<Entry<T>> {
        /**
         * Creates new inventory instance
         *
         * @param player Player to create inventory for
         * @param inventory Current inventory page
         * @return Created inventory or null if inventory cannot be created
         * @since 2.0.0
         */
        abstract fun createNew(
            player: HumanEntity,
            inventory: Entry<T>,
        ): T?

        final override fun refresh(
            player: HumanEntity,
            predicate: (Entry<T>) -> Boolean,
        ) = refresh(player, RefreshBehavior.OpenFirst, predicate)

        final override fun refresh(
            player: HumanEntity,
            inventory: Entry<T>,
        ) = refresh(player, inventory, RefreshBehavior.OpenFirst)

        /**
         * Refreshes inventory for a player if predicate matches
         *
         * @param player Player to refresh inventory for
         * @param behavior Refresh behavior
         * @param predicate Condition for refresh
         * @return True if inventory was refreshed
         * @since 2.0.0
         */
        fun refresh(
            player: HumanEntity,
            behavior: RefreshBehavior,
            predicate: (Entry<T>) -> Boolean = { true },
        ): Boolean {
            val inventory = getOpenInventoryPaginated(clazz, player) ?: return false
            if (predicate(inventory).not()) return false
            refresh(player, inventory, behavior)
            return true
        }

        /**
         * Refreshes specific inventory page for a player
         *
         * @param player Player to refresh inventory for
         * @param inventory Current inventory page
         * @param behavior Refresh behavior
         * @since 2.0.0
         */
        fun refresh(
            player: HumanEntity,
            inventory: Entry<T>,
            behavior: RefreshBehavior = RefreshBehavior.OpenFirst,
        ) {
            val newInventory = createNew(player, inventory)
            if (newInventory != null) {
                val page =
                    when (behavior) {
                        RefreshBehavior.Keep -> inventory.page
                        RefreshBehavior.OpenFirst -> 0
                    }
                newInventory.open(player, page)
            } else {
                player.closeInventory()
            }
        }

        final override fun refreshAll(predicate: (Player, Entry<T>) -> Boolean) = refreshAll(RefreshBehavior.OpenFirst, predicate)

        /**
         * Refreshes inventory for all matching viewers
         *
         * @param behavior Refresh behavior
         * @param predicate Condition for refresh
         * @since 2.0.0
         */
        fun refreshAll(
            behavior: RefreshBehavior = RefreshBehavior.OpenFirst,
            predicate: (Player, Entry<T>) -> Boolean = { _, _ -> true },
        ) {
            getAllViewersPaginated(clazz)
                .filter { (player, inventory) ->
                    predicate(player, inventory)
                }.forEach { (player, inventory) ->
                    refresh(player, inventory, behavior)
                }
        }

        /**
         * Defines how inventory should be refreshed
         *
         * @since 2.0.0
         */
        enum class RefreshBehavior {
            /**
             * Keep current page when refreshing
             *
             * @since 2.0.0
             */
            Keep,

            /**
             * Open first page when refreshing
             *
             * @since 2.0.0
             */
            OpenFirst,
        }
    }
}
