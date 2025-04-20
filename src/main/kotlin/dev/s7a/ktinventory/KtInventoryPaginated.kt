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

abstract class KtInventoryPaginated(
    private val plugin: Plugin,
    line: Int,
) : KtInventoryBase(line) {
    private val paginates = mutableListOf<Int>()

    abstract val entries: List<KtInventoryButton<Entry<KtInventoryPaginated>>>

    private val pages = ConcurrentHashMap<Int, Entry<KtInventoryPaginated>>()

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
        onClick: (KtInventoryButton.ClickEvent<Entry<KtInventoryPaginated>>) -> Unit,
    ) = KtInventoryButton(itemStack, onClick)

    fun nextPageButton(
        slot: Int,
        itemStack: ItemStack,
        onClick: (KtInventoryButton.ClickEvent<Entry<KtInventoryPaginated>>) -> Unit = {},
    ) {
        nextPageButton(slot, createButton(itemStack, onClick))
    }

    fun nextPageButton(
        slot: Int,
        item: KtInventoryButton<Entry<KtInventoryPaginated>>,
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
        onClick: (KtInventoryButton.ClickEvent<Entry<KtInventoryPaginated>>) -> Unit = {},
    ) {
        previousPageButton(slot, createButton(itemStack, onClick))
    }

    fun previousPageButton(
        slot: Int,
        item: KtInventoryButton<Entry<KtInventoryPaginated>>,
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

    class Entry<T : KtInventoryPaginated>(
        val paginated: T,
        val page: Int,
        val lastPage: Int,
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

    abstract class Refreshable<T : KtInventoryPaginated>(
        val clazz: KClass<T>,
    ) {
        abstract fun createNew(
            player: HumanEntity,
            inventory: Entry<T>,
        ): T?

        fun refresh(
            player: HumanEntity,
            behavior: RefreshBehavior = RefreshBehavior.OpenFirst,
            predicate: (Entry<T>) -> Boolean = { true },
        ): Boolean {
            val inventory = getOpenInventoryPaginated(clazz, player) ?: return false
            if (predicate(inventory).not()) return false
            refresh(player, inventory, behavior)
            return true
        }

        private fun refresh(
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

        enum class RefreshBehavior {
            Keep,
            OpenFirst,
        }
    }
}
