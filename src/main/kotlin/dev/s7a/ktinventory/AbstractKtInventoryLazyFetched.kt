package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.util.getTopInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack

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
abstract class AbstractKtInventoryLazyFetched<T : AbstractKtInventoryLazyFetched<T, C, D>, C : Any, D>(
    private val context: KtInventoryPluginContext.LazyFetchable,
    line: Int,
) : KtInventoryBase(line) {
    private val paginates = mutableListOf<Int>()

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
    ): AbstractKtInventoryFetched.Page<C, D>

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
        this.paginates.addAll(slots)
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
        this@AbstractKtInventoryLazyFetched.buttons.forEach { (slot, item) ->
            entry.button(slot, item)
        }
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
    abstract class Entry<T : AbstractKtInventoryLazyFetched<T, C, D>, C : Any, D>(
        val paginated: T,
        val condition: C,
    ) : AbstractKtInventory(paginated.context, paginated.line) {
        internal var page: AbstractKtInventoryFetched.Page<C, D>? = null

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
}
