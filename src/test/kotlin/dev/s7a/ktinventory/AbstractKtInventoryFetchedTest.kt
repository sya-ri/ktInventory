package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.util.getTopInventoryPaginated
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AbstractKtInventoryFetchedTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `open fetches entries by offset condition and places them in pagination slots`() {
        val player = server.addPlayer()
        val inventory = OffsetFetchedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 2)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryFetched.Entry<*, *>
        assertSame(inventory, entry.paginated)
        assertSame(inventory, getTopInventoryPaginated<OffsetFetchedInventory>(player))
        assertEquals(2, entry.condition)
        assertEquals(listOf(2), inventory.fetchedConditions)
        assertNull(player.openInventory.topInventory.getItem(0))
        assertEquals(
            Material.DIAMOND,
            player.openInventory.topInventory
                .getItem(2)
                ?.type,
        )
        assertEquals(
            Material.GOLD_INGOT,
            player.openInventory.topInventory
                .getItem(4)
                ?.type,
        )
    }

    @Test
    fun `navigation buttons open previous and next fetched conditions`() {
        val player = server.addPlayer()
        val inventory = OffsetFetchedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        player.clickInventorySlot(8)
        val next = player.openInventory.topInventory.holder as AbstractKtInventoryFetched.Entry<*, *>
        player.clickInventorySlot(7)
        val previous = player.openInventory.topInventory.holder as AbstractKtInventoryFetched.Entry<*, *>

        assertEquals(2, next.condition)
        assertEquals(0, previous.condition)
        assertEquals(listOf(0, 2, 0), inventory.fetchedConditions)
    }

    @Test
    fun `open fetches entries by cursor condition`() {
        val player = server.addPlayer()
        val inventory = CursorFetchedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        player.clickInventorySlot(8)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryFetched.Entry<*, *>
        assertEquals("cursor-2", entry.condition)
        assertEquals(listOf("start", "cursor-2"), inventory.fetchedConditions)
        assertEquals(
            Material.DIAMOND,
            player.openInventory.topInventory
                .getItem(2)
                ?.type,
        )
        assertEquals(
            Material.GOLD_INGOT,
            player.openInventory.topInventory
                .getItem(4)
                ?.type,
        )
    }

    @Test
    fun `fixed buttons cannot share fetched pagination slots`() {
        val inventory = OffsetFetchedInventory(KtInventoryPluginContext(plugin))
        val otherInventory = OffsetFetchedWithoutSlotsInventory(KtInventoryPluginContext(plugin))

        kotlin.test.assertFailsWith<IllegalArgumentException> {
            inventory.button(2, ItemStack(Material.EMERALD))
        }
        otherInventory.button(2, ItemStack(Material.EMERALD))
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            otherInventory.paginateSlot(2)
        }
    }

    private class OffsetFetchedInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryFetched<Int>(context, 1) {
        private val materials =
            listOf(
                Material.STONE,
                Material.DIRT,
                Material.DIAMOND,
                Material.GOLD_INGOT,
            )
        val fetchedConditions = mutableListOf<Int>()

        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): Page<Int, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetched<Int>, Int>>> {
            fetchedConditions += condition
            val nextCondition = condition + limit
            val previousCondition = condition - limit
            return Page(
                entries =
                    materials
                        .drop(condition)
                        .take(limit)
                        .map { createButton(ItemStack(it)) {} },
                previousCondition = previousCondition.takeIf { it >= 0 },
                nextCondition = nextCondition.takeIf { it < materials.size },
            )
        }

        override fun title(condition: Int) = "Offset $condition"

        init {
            paginateSlot(2, 4)
            previousPageButton(7, ItemStack(Material.ARROW))
            nextPageButton(8, ItemStack(Material.ARROW))
        }
    }

    private class OffsetFetchedWithoutSlotsInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryFetched<Int>(context, 1) {
        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): Page<Int, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetched<Int>, Int>>> = Page(emptyList())

        override fun title(condition: Int) = "Offset $condition"
    }

    private class CursorFetchedInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryFetched<String>(context, 1) {
        val fetchedConditions = mutableListOf<String>()

        override val initialCondition = "start"

        override fun fetch(
            condition: String,
            limit: Int,
        ): Page<String, KtInventoryButton<AbstractKtInventoryFetched.Entry<KtInventoryFetched<String>, String>>> {
            fetchedConditions += condition
            return when (condition) {
                "start" -> {
                    Page(
                        entries =
                            listOf(Material.STONE, Material.DIRT)
                                .take(limit)
                                .map { createButton(ItemStack(it)) {} },
                        nextCondition = "cursor-2",
                    )
                }

                else -> {
                    Page(
                        entries =
                            listOf(Material.DIAMOND, Material.GOLD_INGOT)
                                .take(limit)
                                .map { createButton(ItemStack(it)) {} },
                        previousCondition = "start",
                    )
                }
            }
        }

        override fun title(condition: String) = condition

        init {
            paginateSlot(2, 4)
            previousPageButton(7, ItemStack(Material.ARROW))
            nextPageButton(8, ItemStack(Material.ARROW))
        }
    }
}
