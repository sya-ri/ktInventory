package dev.s7a.ktinventory

import org.bukkit.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

abstract class KtInventory(
    protected val plugin: Plugin,
    protected val line: Int,
) : InventoryHolder {
    init {
        require(line in 1..6) { "line must be between 1 and 6" }
    }

    abstract fun title(): String

    private val bukkitInventory by lazy {
        plugin.server.createInventory(this, line * 9, ChatColor.translateAlternateColorCodes('&', title()))
    }

    internal val items = mutableMapOf<Int, KtInventoryItem<KtInventory>>()

    protected fun button(
        slot: Int,
        itemStack: ItemStack,
        onClick: (InventoryClickEvent, KtInventory) -> Unit = { _, _ -> },
    ) {
        button(slot, KtInventoryItem(itemStack, onClick))
    }

    protected fun button(
        slot: Int,
        item: KtInventoryItem<KtInventory>,
    ) {
        require(slot in 0 until line * 9) { "slot must be between 0 and ${line * 9}" }
        bukkitInventory.setItem(slot, item.itemStack)
        items[slot] = item
    }

    open fun open(player: HumanEntity) {
        KtInventoryHandler.register(plugin)
        player.openInventory(bukkitInventory)
    }

    open fun onOpen(event: InventoryOpenEvent) {}

    open fun onClick(event: InventoryClickEvent) {}

    fun onClick(
        slot: Int,
        event: InventoryClickEvent,
    ) {
        val item = items[slot] ?: return
        item.onClick(event, this)
        event.isCancelled = true
    }

    open fun onClose(event: InventoryCloseEvent) {}

    override fun getInventory() = bukkitInventory

    abstract class Paginated(
        plugin: Plugin,
        line: Int,
    ) : KtInventory(plugin, line) {
        private val slots = mutableListOf<Int>()
        abstract val entries: List<KtInventoryItem<Entry>>

        override fun title() = title(0, 0)

        abstract fun title(
            page: Int,
            lastPage: Int,
        ): String

        fun paginateSlot(vararg slot: Int) {
            paginateSlot(slot.toList())
        }

        fun paginateSlot(slot: Iterable<Int>) {
            slots.addAll(slot)
        }

        fun nextPageButton(
            slot: Int,
            itemStack: ItemStack,
            onClick: (InventoryClickEvent, Entry) -> Unit = { _, _ -> },
        ) {
            nextPageButton(slot, KtInventoryItem(itemStack, onClick))
        }

        fun nextPageButton(
            slot: Int,
            item: KtInventoryItem<Entry>,
        ) {
            button(
                slot,
                item.join { event, inventory ->
                    inventory.openNextPage(event.whoClicked)
                },
            )
        }

        fun previousPageButton(
            slot: Int,
            itemStack: ItemStack,
            onClick: (InventoryClickEvent, Entry) -> Unit = { _, _ -> },
        ) {
            button(slot, KtInventoryItem(itemStack, onClick))
        }

        fun previousPageButton(
            slot: Int,
            item: KtInventoryItem<Entry>,
        ) {
            button(
                slot,
                item.join { event, inventory ->
                    inventory.openPreviousPage(event.whoClicked)
                },
            )
        }

        fun open(
            player: HumanEntity,
            page: Int,
        ) {
            val slots = this.slots
            val chunked = entries.chunked(slots.size)
            val lastPage = chunked.lastIndex
            val chunk =
                when {
                    page == 0 && chunked.isEmpty() -> emptyList()
                    page < 0 -> return open(player, 0)
                    lastPage < page -> return open(player, lastPage)
                    else -> chunked[page]
                }
            val items = super.items
            Entry(this, page, lastPage)
                .apply {
                    slots.forEachIndexed { index, slot ->
                        chunk.getOrNull(index)?.let {
                            button(slot, it)
                        }
                    }
                    items.forEach { (slot, item) ->
                        button(slot, item)
                    }
                }.open(player)
        }

        override fun open(player: HumanEntity) {
            open(player, 0)
        }

        class Entry(
            private val paginated: Paginated,
            private val page: Int,
            private val lastPage: Int,
        ) : KtInventory(paginated.plugin, paginated.line) {
            override fun title() = paginated.title(page, lastPage)

            fun openNextPage(player: HumanEntity) {
                paginated.open(player, page + 1)
            }

            fun openPreviousPage(player: HumanEntity) {
                paginated.open(player, page - 1)
            }
        }
    }
}
