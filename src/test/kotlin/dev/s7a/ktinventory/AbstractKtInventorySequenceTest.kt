package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.util.getTopInventoryPaginated
import dev.s7a.ktinventory.util.getTopInventorySequenceEntry
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class AbstractKtInventorySequenceTest {
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
    fun `open shows requested page without materializing every entry`() {
        val player = server.addPlayer()
        val inventory = TestSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 2)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        assertSame(inventory, entry.paginated)
        assertEquals(2, entry.page)
        assertEquals(6, inventory.materializedEntries)
        assertSame(inventory, getTopInventoryPaginated<TestSequenceInventory>(player))
        assertSame(entry as Any?, getTopInventorySequenceEntry<TestSequenceInventory>(player))
        assertNotNull(player.openInventory.topInventory.getItem(0))
    }

    @Test
    fun `open places requested sequence entries in pagination slots`() {
        val player = server.addPlayer()
        val inventory = SlottedSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

        assertEquals(4, inventory.materializedEntries)
        assertSame(inventory, (player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>).paginated)
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
    fun `fixed buttons cannot share sequence pagination slots`() {
        val inventory = SlottedSequenceInventory(KtInventoryPluginContext(plugin))
        val otherInventory = TestSequenceInventory(KtInventoryPluginContext(plugin))

        assertFailsWith<IllegalArgumentException> {
            inventory.button(2, ItemStack(Material.EMERALD))
        }
        otherInventory.button(2, ItemStack(Material.EMERALD))
        assertFailsWith<IllegalArgumentException> {
            otherInventory.paginateSlot(2)
        }
    }

    @Test
    fun `open creates skipped sequence pages while consuming entries once`() {
        val player = server.addPlayer()
        val inventory = TestSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 2)
        inventory.open(player, 2)

        assertEquals(listOf(0, 1, 2), inventory.createdPageTitles)
        assertEquals(6, inventory.materializedEntries)

        inventory.open(player, 0)

        assertEquals(listOf(0, 1, 2), inventory.createdPageTitles)
        assertEquals(6, inventory.materializedEntries)
    }

    @Test
    fun `opening an earlier page after a later page reuses cached sequence entries`() {
        val player = server.addPlayer()
        val inventory = ReiterableSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 2)
        inventory.open(player, 0)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        assertEquals(0, entry.page)
        assertEquals(1, inventory.entriesAccessCount)
        assertEquals(listOf(6), inventory.materializedEntryCounts)
        assertEquals(
            Material.DIAMOND,
            player.openInventory.topInventory
                .getItem(0)
                ?.type,
        )
    }

    @Test
    fun `opening pages out of order works with a one shot sequence`() {
        val player = server.addPlayer()
        val inventory = OneShotSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 2)
        inventory.open(player, 0)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        assertEquals(0, entry.page)
        assertEquals(
            Material.DIAMOND,
            player.openInventory.topInventory
                .getItem(0)
                ?.type,
        )
    }

    @Test
    fun `open clamps page below first page`() {
        val player = server.addPlayer()
        val inventory = TestSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, -1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        assertEquals(0, entry.page)
    }

    @Test
    fun `open requires paginate slots`() {
        val player = server.addPlayer()
        val inventory = SequenceWithoutSlots(KtInventoryPluginContext(plugin))

        assertFailsWith<IllegalArgumentException> {
            inventory.open(player)
        }
    }

    @Test
    fun `entry navigation opens previous and next pages`() {
        val player = server.addPlayer()
        val inventory = TestSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)
        (player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>).openNextPage(player)
        val next = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        next.openPreviousPage(player)
        val previous = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>

        assertEquals(2, next.page)
        assertEquals(1, previous.page)
    }

    @Test
    fun `navigation buttons open previous and next pages`() {
        val player = server.addPlayer()
        val inventory = NavigationSequenceInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 0)
        player.clickInventorySlot(8)
        val next = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        player.clickInventorySlot(7)
        val previous = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>

        assertEquals(1, next.page)
        assertEquals(0, previous.page)
    }

    private class TestSequenceInventory(
        context: KtInventoryPluginContext,
    ) : KtInventorySequence(context, 1) {
        var materializedEntries = 0
            private set
        val createdPageTitles = mutableListOf<Int>()

        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>>
            get() =
                sequence {
                    repeat(10) {
                        materializedEntries += 1
                        yield(createButton(ItemStack(Material.STONE)) {})
                    }
                }

        override fun title(page: Int): String {
            createdPageTitles += page
            return "Test ${page + 1}"
        }

        init {
            paginateSlot(0, 1)
        }
    }

    private class SequenceWithoutSlots(
        context: KtInventoryPluginContext,
    ) : KtInventorySequence(context, 1) {
        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>>
            get() = emptySequence()

        override fun title(page: Int) = "Test ${page + 1}"
    }

    private class SlottedSequenceInventory(
        context: KtInventoryPluginContext,
    ) : KtInventorySequence(context, 1) {
        var materializedEntries = 0
            private set

        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>>
            get() =
                sequence {
                    listOf(
                        Material.STONE,
                        Material.DIRT,
                        Material.DIAMOND,
                        Material.GOLD_INGOT,
                    ).forEach {
                        materializedEntries += 1
                        yield(createButton(ItemStack(it)) {})
                    }
                }

        override fun title(page: Int) = "Slotted"

        init {
            paginateSlot(2, 4)
        }
    }

    private class ReiterableSequenceInventory(
        context: KtInventoryPluginContext,
    ) : KtInventorySequence(context, 1) {
        var entriesAccessCount = 0
            private set
        val materializedEntryCounts = mutableListOf<Int>()

        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>>
            get() {
                entriesAccessCount += 1
                var materializedEntries = 0
                return sequence {
                    repeat(10) { index ->
                        materializedEntries += 1
                        val material =
                            if (index == 0) {
                                Material.DIAMOND
                            } else {
                                Material.STONE
                            }
                        yield(createButton(ItemStack(material)) {})
                    }
                }.onEach {
                    materializedEntryCounts[materializedEntryCounts.lastIndex] = materializedEntries
                }.also {
                    materializedEntryCounts += 0
                }
            }

        override fun title(page: Int) = "Reiterable ${page + 1}"

        init {
            paginateSlot(0, 1)
        }
    }

    private class OneShotSequenceInventory(
        context: KtInventoryPluginContext,
    ) : KtInventorySequence(context, 1) {
        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>> =
            (0 until 10)
                .map { index ->
                    val material =
                        if (index == 0) {
                            Material.DIAMOND
                        } else {
                            Material.STONE
                        }
                    createButton(ItemStack(material)) {}
                }.iterator()
                .asSequence()

        override fun title(page: Int) = "OneShot ${page + 1}"

        init {
            paginateSlot(0, 1)
        }
    }

    private class NavigationSequenceInventory(
        context: KtInventoryPluginContext,
    ) : KtInventorySequence(context, 1) {
        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>>
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = "Navigation"

        init {
            paginateSlot(0, 1)
            previousPageButton(7, ItemStack(Material.ARROW))
            nextPageButton(8, ItemStack(Material.ARROW))
        }
    }
}
