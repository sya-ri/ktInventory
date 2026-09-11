package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.components.KtInventoryPagedStorable
import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.util.getTopInventoryPaginatedSequenceEntry
import dev.s7a.ktinventory.util.getViewersPaginatedSequenceEntry
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Base class for sequence-backed paginated inventories.
 *
 * Unlike [AbstractKtInventoryPaginated], this class does not expose the last page number.
 *
 * @param T Type of the sequence-backed inventory
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class AbstractKtInventoryPaginatedSequence<T : AbstractKtInventoryPaginatedSequence<T>>(
    private val context: KtInventoryPluginContext,
    line: Int,
) : KtInventoryBase(line) {
    /**
     * Sequence of entries to be paginated.
     *
     * @since 2.2.0
     */
    abstract val entries: Sequence<KtInventoryButton<Entry<T>>>

    private val iterator by lazy {
        entries.iterator()
    }

    private var nextPage = 0

    private val paginates = mutableListOf<Int>()

    private val pages = ConcurrentHashMap<Int, Entry<T>>()

    private val _pagedStorables = mutableSetOf<KtInventoryPagedStorable<Entry<T>>>()

    /**
     * Set of all paged storables in this inventory.
     *
     * @since 2.2.0
     */
    val pagedStorables
        get() = _pagedStorables.toSet()

    /**
     * Creates a new inventory page.
     *
     * @param page Page number
     * @return Created inventory page
     * @since 2.2.0
     */
    protected abstract fun createEntry(page: Int): Entry<T>

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        require(slot !in paginates) { "button slot must not be used as a pagination slot (actual: $slot)" }
        require(_pagedStorables.none { slot in it.slots }) {
            "button slot must not be used as a storable slot (actual: $slot)"
        }
        super.button(slot, item)
    }

    /**
     * Sets slots to be used for pagination.
     *
     * @param slots Slot numbers
     * @since 2.2.0
     */
    fun paginateSlot(vararg slots: Int) {
        paginateSlot(slots.toList())
    }

    /**
     * Sets slots to be used for pagination.
     *
     * @param slots Slot numbers
     * @since 2.2.0
     */
    fun paginateSlot(slots: Iterable<Int>) {
        val buttons = this.buttons
        require(slots.none { it in buttons }) { "pagination slots must not contain fixed button slots" }
        require(slots.all { slot -> _pagedStorables.none { slot in it.slots } }) {
            "pagination slots must not contain storable slots"
        }
        this.paginates.addAll(slots)
    }

    /**
     * Adds a storable that is applied to each created page entry.
     *
     * @param slots Slot numbers to manage
     * @param initialize Provides initial items for each page entry
     * @param onPreClick Handler called before click events
     * @param onClick Handler for click events
     * @param onPreDrag Handler called before drag events
     * @param onDrag Handler for drag events
     * @param save Handler to save page entry state
     * @since 2.2.0
     */
    fun storable(
        slots: Iterable<Int>,
        initialize: Entry<T>.() -> List<ItemStack?> = { emptyList() },
        onPreClick: Entry<T>.(KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onClick: Entry<T>.(KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: Entry<T>.(KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onDrag: Entry<T>.(KtInventoryStorable.DragEvent) -> Unit = {},
        save: Entry<T>.(List<ItemStack?>) -> Unit = {},
    ): KtInventoryPagedStorable<Entry<T>> {
        val slots = slots.toList()
        require(slots.none { it in paginates }) { "storable slots must not contain pagination slots" }
        val buttons = this.buttons
        require(slots.none { it in buttons }) { "storable slots must not contain fixed button slots" }
        val pagedStorable =
            KtInventoryPagedStorable(
                slots = slots,
                initialize = initialize,
                onPreClick = onPreClick,
                onClick = onClick,
                onPreDrag = onPreDrag,
                onDrag = onDrag,
                save = save,
            )
        _pagedStorables += pagedStorable
        pages.values.forEach(pagedStorable::applyTo)
        return pagedStorable
    }

    /**
     * Adds a storable that is applied to each created page entry.
     *
     * @param slots Slot numbers to manage
     * @since 2.2.0
     */
    fun storable(
        vararg slots: Int,
        initialize: Entry<T>.() -> List<ItemStack?> = { emptyList() },
        onPreClick: Entry<T>.(KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onClick: Entry<T>.(KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: Entry<T>.(KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onDrag: Entry<T>.(KtInventoryStorable.DragEvent) -> Unit = {},
        save: Entry<T>.(List<ItemStack?>) -> Unit = {},
    ) {
        storable(slots.toList(), initialize, onPreClick, onClick, onPreDrag, onDrag, save)
    }

    /**
     * Creates a new inventory button.
     *
     * @param itemStack Item to display
     * @param onClick Click handler
     * @return Created button
     * @since 2.2.0
     */
    fun createButton(
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T>>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    /**
     * Adds a button to navigate to the next page.
     *
     * @param slot Button slot number
     * @param itemStack Item to display
     * @param onClick Additional click handler
     * @since 2.2.0
     */
    fun nextPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T>>) -> Unit = {},
    ) {
        nextPageButton(slot, createButton(itemStack, onClick))
    }

    /**
     * Adds a button to navigate to the next page.
     *
     * @param slot Button slot number
     * @param item Button to use
     * @since 2.2.0
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
     * Adds a button to navigate to the previous page.
     *
     * @param slot Button slot number
     * @param itemStack Item to display
     * @param onClick Additional click handler
     * @since 2.2.0
     */
    fun previousPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T>>) -> Unit = {},
    ) {
        previousPageButton(slot, createButton(itemStack, onClick))
    }

    /**
     * Adds a button to navigate to the previous page.
     *
     * @param slot Button slot number
     * @param item Button to use
     * @since 2.2.0
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
     * Opens specific page of the inventory for a player.
     *
     * @param player Player to open inventory for
     * @param page Page number to open
     * @since 2.2.0
     */
    fun open(
        player: HumanEntity,
        page: Int,
    ) {
        if (page < 0) {
            open(player, 0)
            return
        }
        val pageSize = paginates.size
        require(pageSize > 0) { "Call paginateSlot before opening a sequence-backed inventory." }
        createPagesUpTo(page).open(player)
    }

    /**
     * Creates missing pages up to the specified page.
     *
     * This consumes [entries] while assigning buttons to pagination slots.
     *
     * @param page Page number to create up to
     * @return Created or cached page for the specified page number
     */
    @Synchronized
    private fun createPagesUpTo(page: Int): Entry<T> {
        while (nextPage <= page) {
            val currentPage = nextPage
            pages[currentPage] =
                createEntry(currentPage)
                    .apply {
                        this@AbstractKtInventoryPaginatedSequence.paginates.forEach { slot ->
                            if (iterator.hasNext()) {
                                button(slot, iterator.next())
                            }
                        }
                        this@AbstractKtInventoryPaginatedSequence.buttons.forEach { (slot, item) ->
                            button(slot, item)
                        }
                        _pagedStorables.forEach { it.applyTo(this) }
                    }
            nextPage += 1
        }
        return pages.getValue(page)
    }

    /**
     * Represents a single page of sequence-backed inventory.
     *
     * @param T Type of the sequence-backed inventory
     * @param paginated Parent inventory
     * @param page Current page number
     * @since 2.2.0
     */
    abstract class Entry<T : AbstractKtInventoryPaginatedSequence<*>>(
        val paginated: T,
        val page: Int,
    ) : AbstractKtInventory(paginated.context, paginated.line) {
        /**
         * Opens next page for a player.
         *
         * @param player Player to open inventory for
         * @since 2.2.0
         */
        fun openNextPage(player: HumanEntity) {
            paginated.open(player, page + 1)
        }

        /**
         * Opens previous page for a player.
         *
         * @param player Player to open inventory for
         * @since 2.2.0
         */
        fun openPreviousPage(player: HumanEntity) {
            paginated.open(player, page - 1)
        }

        override fun onOpen(event: InventoryOpenEvent) = paginated.onOpen(event)

        override fun onClick(event: InventoryClickEvent) = paginated.onClick(event)

        override fun onClose(event: InventoryCloseEvent) = paginated.onClose(event)
    }

    /**
     * Base class for refreshable sequence-backed inventories.
     *
     * @param T Type of the sequence-backed inventory
     * @param clazz Class of the sequence-backed inventory
     * @since 2.2.0
     */
    abstract class Refreshable<T : AbstractKtInventoryPaginatedSequence<*>>(
        val clazz: KClass<T>,
    ) : RefreshableInventory<Entry<T>> {
        /**
         * Creates new inventory instance.
         *
         * @param player Player to create inventory for
         * @param inventory Current inventory page
         * @return Created inventory or null if inventory cannot be created
         * @since 2.2.0
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
         * Refreshes inventory for a player if predicate matches.
         *
         * @param player Player to refresh inventory for
         * @param behavior Refresh behavior
         * @param predicate Condition for refresh
         * @return True if inventory was refreshed
         * @since 2.2.0
         */
        fun refresh(
            player: HumanEntity,
            behavior: RefreshBehavior,
            predicate: (Entry<T>) -> Boolean = { true },
        ): Boolean {
            val inventory = getTopInventoryPaginatedSequenceEntry(clazz, player) ?: return false
            if (predicate(inventory).not()) return false
            refresh(player, inventory, behavior)
            return true
        }

        /**
         * Refreshes specific inventory page for a player.
         *
         * @param player Player to refresh inventory for
         * @param inventory Current inventory page
         * @param behavior Refresh behavior
         * @since 2.2.0
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
         * Refreshes inventory for all matching viewers.
         *
         * @param behavior Refresh behavior
         * @param predicate Condition for refresh
         * @since 2.2.0
         */
        fun refreshAll(
            behavior: RefreshBehavior = RefreshBehavior.OpenFirst,
            predicate: (Player, Entry<T>) -> Boolean = { _, _ -> true },
        ) {
            getViewersPaginatedSequenceEntry(clazz)
                .filter { (player, inventory) ->
                    predicate(player, inventory)
                }.forEach { (player, inventory) ->
                    refresh(player, inventory, behavior)
                }
        }

        /**
         * Defines how inventory should be refreshed.
         *
         * @since 2.2.0
         */
        enum class RefreshBehavior {
            /**
             * Keep current page when refreshing.
             *
             * @since 2.2.0
             */
            Keep,

            /**
             * Open first page when refreshing.
             *
             * @since 2.2.0
             */
            OpenFirst,
        }
    }
}
