package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack

/**
 * Base class for inventories that fetch entries by display condition.
 *
 * @param T Type of the fetched inventory
 * @param C Type of the display condition
 * @param context Plugin context
 * @param line Number of inventory rows
 * @since 2.2.0
 */
abstract class AbstractKtInventoryFetched<T : AbstractKtInventoryFetched<T, C>, C : Any>(
    private val context: KtInventoryPluginContext,
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
     * Fetches entries for the specified display condition.
     *
     * @param condition Display condition
     * @param limit Maximum number of entries to fetch
     * @return Fetched page data
     * @since 2.2.0
     */
    protected abstract fun fetch(
        condition: C,
        limit: Int,
    ): Page<C, KtInventoryButton<Entry<T, C>>>

    /**
     * Creates a new inventory page.
     *
     * @param condition Display condition
     * @param page Fetched page data
     * @return Created inventory page
     * @since 2.2.0
     */
    protected abstract fun createEntry(
        condition: C,
        page: Page<C, KtInventoryButton<Entry<T, C>>>,
    ): Entry<T, C>

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
        onClick: (KtInventoryButton.ClickEvent<Entry<T, C>>) -> Unit,
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
        onClick: (KtInventoryButton.ClickEvent<Entry<T, C>>) -> Unit = {},
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
        item: KtInventoryButton<Entry<T, C>>,
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
        onClick: (KtInventoryButton.ClickEvent<Entry<T, C>>) -> Unit = {},
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
        item: KtInventoryButton<Entry<T, C>>,
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
     * Opens the inventory for a player using the specified display condition.
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
        require(limit > 0) { "Call paginateSlot before opening a fetched inventory." }
        val page = fetch(condition, limit)
        createEntry(condition, page)
            .apply {
                this@AbstractKtInventoryFetched.paginates.forEachIndexed { index, slot ->
                    page.entries.getOrNull(index)?.let {
                        button(slot, it)
                    }
                }
                this@AbstractKtInventoryFetched.buttons.forEach { (slot, item) ->
                    button(slot, item)
                }
            }.open(player)
    }

    /**
     * Fetched page data.
     *
     * @param C Type of the display condition
     * @param E Type of the entry
     * @property entries Entries to place in pagination slots
     * @property previousCondition Condition for the previous page, or null if unavailable
     * @property nextCondition Condition for the next page, or null if unavailable
     * @since 2.2.0
     */
    data class Page<C : Any, E>(
        val entries: List<E>,
        val previousCondition: C? = null,
        val nextCondition: C? = null,
    )

    /**
     * Represents a single fetched inventory page.
     *
     * @param T Type of the fetched inventory
     * @param C Type of the display condition
     * @param paginated Parent inventory
     * @param condition Current display condition
     * @param page Fetched page data
     * @since 2.2.0
     */
    abstract class Entry<T : AbstractKtInventoryFetched<T, C>, C : Any>(
        val paginated: T,
        val condition: C,
        val page: Page<C, KtInventoryButton<Entry<T, C>>>,
    ) : AbstractKtInventory(paginated.context, paginated.line) {
        /**
         * Opens the next page for a player if [Page.nextCondition] is available.
         *
         * @param player Player to open inventory for
         * @since 2.2.0
         */
        fun openNextPage(player: HumanEntity) {
            page.nextCondition?.let { paginated.open(player, it) }
        }

        /**
         * Opens the previous page for a player if [Page.previousCondition] is available.
         *
         * @param player Player to open inventory for
         * @since 2.2.0
         */
        fun openPreviousPage(player: HumanEntity) {
            page.previousCondition?.let { paginated.open(player, it) }
        }

        override fun onOpen(event: InventoryOpenEvent) = paginated.onOpen(event)

        override fun onClick(event: InventoryClickEvent) = paginated.onClick(event)

        override fun onClose(event: InventoryCloseEvent) = paginated.onClose(event)
    }
}
