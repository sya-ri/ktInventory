package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.components.KtInventoryPagedStorable
import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.util.getTopInventory
import dev.s7a.ktinventory.util.getTopInventoryPaginatedLazyFetchedEntry
import dev.s7a.ktinventory.util.getViewersPaginatedLazyFetchedEntry
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * Base class for inventories that open immediately and populate entries after data is fetched asynchronously.
 *
 * @param T Type of the lazy fetched inventory
 * @param C Type of the display condition
 * @param D Type of the fetched data
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class AbstractKtInventoryPaginatedLazyFetched<T : AbstractKtInventoryPaginatedLazyFetched<T, C, D>, C : Any, D>(
    private val context: KtInventoryPluginContext.LazyFetchable,
    line: Int,
) : KtInventoryBase(line) {
    private val paginates = mutableListOf<Int>()

    private val _pagedStorables = mutableSetOf<KtInventoryPagedStorable<Entry<T, C, D>>>()

    /**
     * Set of all paged storables in this inventory.
     *
     * @since 2.2.0
     */
    val pagedStorables
        get() = _pagedStorables.toSet()

    /**
     * Initial display condition used by [open].
     *
     * @since 2.2.0
     */
    abstract val initialCondition: C

    /**
     * Fetches data for the specified display condition.
     *
     * This function is executed asynchronously. Do not call Bukkit APIs from this function.
     *
     * @param condition Display condition
     * @param limit Maximum number of entries to fetch
     * @return Fetched page data
     * @since 2.2.0
     */
    protected abstract fun fetch(
        condition: C,
        limit: Int,
    ): AbstractKtInventoryPaginatedFetched.Page<C, D>

    /**
     * Creates a button for fetched data.
     *
     * This function is executed on the server main thread.
     *
     * @param data Fetched data
     * @return Created button
     * @since 2.2.0
     */
    protected abstract fun createButton(data: D): KtInventoryButton<Entry<T, C, D>>

    /**
     * Creates a new inventory page.
     *
     * @param condition Display condition
     * @return Created inventory page
     * @since 2.2.0
     */
    protected abstract fun createEntry(condition: C): Entry<T, C, D>

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
     * Adds a storable that is applied to each created condition entry.
     *
     * @param slots Slot numbers to manage
     * @param initialize Provides initial items for each condition entry
     * @param onPreClick Handler called before click events
     * @param onClick Handler for click events
     * @param onPreDrag Handler called before drag events
     * @param onDrag Handler for drag events
     * @param save Handler to save condition entry state
     * @since 2.2.0
     */
    fun storable(
        slots: Iterable<Int>,
        initialize: Entry<T, C, D>.() -> List<ItemStack?> = { emptyList() },
        onPreClick: Entry<T, C, D>.(KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onClick: Entry<T, C, D>.(KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: Entry<T, C, D>.(KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onDrag: Entry<T, C, D>.(KtInventoryStorable.DragEvent) -> Unit = {},
        save: Entry<T, C, D>.(List<ItemStack?>) -> Unit = {},
    ): KtInventoryPagedStorable<Entry<T, C, D>> {
        val slots = slots.toList()
        require(slots.none { it in paginates }) { "storable slots must not contain pagination slots" }
        val buttons = this.buttons
        require(slots.none { it in buttons }) { "storable slots must not contain fixed button slots" }
        return KtInventoryPagedStorable(
            slots = slots,
            initialize = initialize,
            onPreClick = onPreClick,
            onClick = onClick,
            onPreDrag = onPreDrag,
            onDrag = onDrag,
            save = save,
        ).also {
            _pagedStorables += it
        }
    }

    /**
     * Adds a storable that is applied to each created condition entry.
     *
     * @param slots Slot numbers to manage
     * @since 2.2.0
     */
    fun storable(
        vararg slots: Int,
        initialize: Entry<T, C, D>.() -> List<ItemStack?> = { emptyList() },
        onPreClick: Entry<T, C, D>.(KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onClick: Entry<T, C, D>.(KtInventoryStorable.ClickEvent) -> Unit = {},
        onPreDrag: Entry<T, C, D>.(KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult = {
            KtInventoryStorable.EventResult.Allow
        },
        onDrag: Entry<T, C, D>.(KtInventoryStorable.DragEvent) -> Unit = {},
        save: Entry<T, C, D>.(List<ItemStack?>) -> Unit = {},
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
        onClick: (KtInventoryButton.ClickEvent<Entry<T, C, D>>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    /**
     * Adds a button to navigate to the next condition.
     *
     * @param slot Button slot number
     * @param itemStack Item to display
     * @param onClick Additional click handler
     * @since 2.2.0
     */
    fun nextPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T, C, D>>) -> Unit = {},
    ) {
        nextPageButton(slot, createButton(itemStack, onClick))
    }

    /**
     * Adds a button to navigate to the next condition.
     *
     * @param slot Button slot number
     * @param item Button to use
     * @since 2.2.0
     */
    fun nextPageButton(
        slot: Int,
        item: KtInventoryButton<Entry<T, C, D>>,
    ) {
        button(
            slot,
            item.join { event ->
                event.inventory.openNextPage(event.player)
            },
        )
    }

    /**
     * Adds a button to navigate to the previous condition.
     *
     * @param slot Button slot number
     * @param itemStack Item to display
     * @param onClick Additional click handler
     * @since 2.2.0
     */
    fun previousPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<T, C, D>>) -> Unit = {},
    ) {
        previousPageButton(slot, createButton(itemStack, onClick))
    }

    /**
     * Adds a button to navigate to the previous condition.
     *
     * @param slot Button slot number
     * @param item Button to use
     * @since 2.2.0
     */
    fun previousPageButton(
        slot: Int,
        item: KtInventoryButton<Entry<T, C, D>>,
    ) {
        button(
            slot,
            item.join { event ->
                event.inventory.openPreviousPage(event.player)
            },
        )
    }

    final override fun open(player: HumanEntity) {
        open(player, initialCondition)
    }

    /**
     * Opens the inventory immediately and populates entries after fetching data asynchronously.
     *
     * @param player Player to open inventory for
     * @param condition Display condition
     * @since 2.2.0
     */
    fun open(
        player: HumanEntity,
        condition: C,
    ) {
        val limit = paginates.size
        require(limit > 0) { "Call paginateSlot before opening a lazy fetched inventory." }
        val entry = createEntry(condition)
        this@AbstractKtInventoryPaginatedLazyFetched.buttons.forEach { (slot, item) ->
            entry.button(slot, item)
        }
        _pagedStorables.forEach { it.applyTo(entry) }
        entry.open(player)
        context.runTaskAsync {
            val page = fetch(condition, limit)
            context.runTask {
                if (getTopInventory<KtInventoryBase>(player) !== entry) return@runTask
                entry.page = page
                paginates.forEachIndexed { index, slot ->
                    page.entries.getOrNull(index)?.let {
                        entry.button(slot, createButton(it))
                    }
                }
            }
        }
    }

    /**
     * Represents a single lazy fetched inventory page.
     *
     * @param T Type of the lazy fetched inventory
     * @param C Type of the display condition
     * @param D Type of the fetched data
     * @param paginated Parent inventory
     * @param condition Current display condition
     * @since 2.2.0
     */
    abstract class Entry<T : AbstractKtInventoryPaginatedLazyFetched<T, C, D>, C : Any, D>(
        val paginated: T,
        val condition: C,
    ) : AbstractKtInventory(paginated.context, paginated.line) {
        internal var page: AbstractKtInventoryPaginatedFetched.Page<C, D>? = null

        /**
         * Opens the next page for a player if the fetched page has a next condition.
         *
         * @param player Player to open inventory for
         * @since 2.2.0
         */
        fun openNextPage(player: HumanEntity) {
            page?.nextCondition?.let { paginated.open(player, it) }
        }

        /**
         * Opens the previous page for a player if the fetched page has a previous condition.
         *
         * @param player Player to open inventory for
         * @since 2.2.0
         */
        fun openPreviousPage(player: HumanEntity) {
            page?.previousCondition?.let { paginated.open(player, it) }
        }

        override fun onOpen(event: InventoryOpenEvent) = paginated.onOpen(event)

        override fun onClick(event: InventoryClickEvent) = paginated.onClick(event)

        override fun onClose(event: InventoryCloseEvent) = paginated.onClose(event)
    }

    /**
     * Base class for refreshable lazy-fetched paginated inventories.
     *
     * @param T Type of the lazy-fetched inventory
     * @param C Type of the display condition
     * @param clazz Class of the lazy-fetched inventory
     * @since 2.2.0
     */
    abstract class Refreshable<T : AbstractKtInventoryPaginatedLazyFetched<*, C, *>, C : Any>(
        val clazz: KClass<T>,
    ) : RefreshableInventory<Entry<*, C, *>> {
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
            inventory: Entry<*, C, *>,
        ): T?

        final override fun refresh(
            player: HumanEntity,
            predicate: (Entry<*, C, *>) -> Boolean,
        ) = refresh(player, RefreshBehavior.OpenFirst, predicate)

        final override fun refresh(
            player: HumanEntity,
            inventory: Entry<*, C, *>,
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
        @Suppress("UNCHECKED_CAST")
        fun refresh(
            player: HumanEntity,
            behavior: RefreshBehavior,
            predicate: (Entry<*, C, *>) -> Boolean = { true },
        ): Boolean {
            val inventory = getTopInventoryPaginatedLazyFetchedEntry(clazz, player) as? Entry<*, C, *> ?: return false
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
            inventory: Entry<*, C, *>,
            behavior: RefreshBehavior = RefreshBehavior.OpenFirst,
        ) {
            val newInventory = createNew(player, inventory)
            if (newInventory != null) {
                when (behavior) {
                    RefreshBehavior.Keep -> newInventory.open(player, inventory.condition)
                    RefreshBehavior.OpenFirst -> newInventory.open(player)
                }
            } else {
                player.closeInventory()
            }
        }

        final override fun refreshAll(predicate: (Player, Entry<*, C, *>) -> Boolean) = refreshAll(RefreshBehavior.OpenFirst, predicate)

        /**
         * Refreshes inventory for all matching viewers.
         *
         * @param behavior Refresh behavior
         * @param predicate Condition for refresh
         * @since 2.2.0
         */
        @Suppress("UNCHECKED_CAST")
        fun refreshAll(
            behavior: RefreshBehavior = RefreshBehavior.OpenFirst,
            predicate: (Player, Entry<*, C, *>) -> Boolean = { _, _ -> true },
        ) {
            getViewersPaginatedLazyFetchedEntry(clazz)
                .mapValues { (_, inventory) -> inventory as Entry<*, C, *> }
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
             * Keep current condition when refreshing.
             *
             * @since 2.2.0
             */
            Keep,

            /**
             * Open the initial condition when refreshing.
             *
             * @since 2.2.0
             */
            OpenFirst,
        }
    }
}
