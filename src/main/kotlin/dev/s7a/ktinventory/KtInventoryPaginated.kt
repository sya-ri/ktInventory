package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

abstract class KtInventoryPaginated(
    private val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line) {
    private val paginates = mutableListOf<Int>()

    abstract val entries: List<KtInventoryButton<Entry>>

    private val pages = ConcurrentHashMap<Int, Entry>()

    abstract fun title(
        page: Int,
        lastPage: Int,
    ): String

    final override fun button(
        slot: Int,
        item: KtInventoryButton<KtInventoryBase>,
    ) {
        super.button(slot, item)
        pages.values.forEach { page ->
            page.button(slot, item)
        }
    }

    fun paginateSlot(vararg slots: Int) {
        paginateSlot(slots.toList())
    }

    fun paginateSlot(slots: Iterable<Int>) {
        this.paginates.addAll(slots)
    }

    fun createButton(
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    fun nextPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry>) -> Unit = {},
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
        onClick: (KtInventoryButton.ClickEvent<Entry>) -> Unit = {},
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

    fun open(
        player: HumanEntity,
        page: Int,
    ) {
        val chunked = entries.chunked(this.paginates.size)
        val lastPage = chunked.lastIndex
        when {
            page < 0 -> open(player, 0)
            page != 0 && lastPage < page -> open(player, lastPage)
            else -> {
                pages
                    .computeIfAbsent(page) {
                        val slots = this.paginates
                        val buttons = this.buttons
                        Entry(this, page, lastPage)
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

        override fun onClick(event: InventoryClickEvent) = paginated.onClick(event)

        override fun onClose(event: InventoryCloseEvent) = paginated.onClose(event)
    }
}
