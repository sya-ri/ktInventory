package dev.s7a.ktinventory

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

abstract class KtInventoryPaginated(
    private val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line) {
    private val paginates = mutableListOf<Int>()

    abstract val entries: List<KtInventoryButton<Entry>>

    abstract fun title(
        page: Int,
        lastPage: Int,
    ): String

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        super.button(slot, item)
    }

    fun paginateSlot(vararg slots: Int) {
        paginateSlot(slots.toList())
    }

    fun paginateSlot(slots: Iterable<Int>) {
        this.paginates.addAll(slots)
    }

    fun nextPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickState<Entry>) -> Unit = {},
    ) {
        nextPageButton(slot, createButton(itemStack, onClick))
    }

    fun nextPageButton(
        slot: Int,
        item: KtInventoryButton<Entry>,
    ) {
        button(
            slot,
            item.join { (inventory, player) ->
                inventory.openNextPage(player)
            },
        )
    }

    fun previousPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickState<Entry>) -> Unit = {},
    ) {
        previousPageButton(slot, createButton(itemStack, onClick))
    }

    fun previousPageButton(
        slot: Int,
        item: KtInventoryButton<Entry>,
    ) {
        button(
            slot,
            item.join { (inventory, player) ->
                inventory.openPreviousPage(player)
            },
        )
    }

    final override fun open(player: HumanEntity) {
        open(player, 0)
    }

    fun createButton(
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickState<Entry>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    fun open(
        player: HumanEntity,
        page: Int,
    ) {
        val slots = this.paginates
        val chunked = entries.chunked(slots.size)
        val lastPage = chunked.lastIndex
        val chunk =
            when {
                page == 0 && chunked.isEmpty() -> emptyList()
                page < 0 -> return open(player, 0)
                lastPage < page -> return open(player, lastPage)
                else -> chunked[page]
            }
        val buttons = this.buttons
        Entry(this, page, lastPage)
            .apply {
                slots.forEachIndexed { index, slot ->
                    chunk.getOrNull(index)?.let {
                        button(slot, it)
                    }
                }
                buttons.forEach { (slot, item) ->
                    button(slot, item)
                }
            }.open(player)
    }

    class Entry(
        private val paginated: KtInventoryPaginated,
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

        override fun onOpen(event: InventoryOpenEvent) = paginated.onOpen(event)

        override fun onClose(event: InventoryCloseEvent) = paginated.onClose(event)
    }
}
