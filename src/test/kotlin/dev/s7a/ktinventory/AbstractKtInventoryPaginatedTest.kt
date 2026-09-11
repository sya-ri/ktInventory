package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryPagedStorable
import dev.s7a.ktinventory.util.getTopInventory
import dev.s7a.ktinventory.util.getTopInventoryPaginatedEntry
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class AbstractKtInventoryPaginatedTest {
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
    fun `open shows requested page to player`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertSame(inventory, entry.paginated)
        assertEquals(1, entry.page)
        assertEquals(2, entry.lastPage)
        assertSame(inventory, getTopInventory<TestPaginatedInventory>(player))
        assertSame(entry as Any?, getTopInventoryPaginatedEntry<TestPaginatedInventory>(player))
        assertNotNull(player.openInventory.topInventory.getItem(0))
    }

    @Test
    fun `open places requested page entries in pagination slots`() {
        val player = server.addPlayer()
        val inventory = SlottedPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

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
    fun `fixed buttons cannot share paginated slots`() {
        val inventory = SlottedPaginatedInventory(KtInventoryPluginContext(plugin))
        val otherInventory = TestPaginatedInventory(KtInventoryPluginContext(plugin))

        kotlin.test.assertFailsWith<IllegalArgumentException> {
            inventory.button(2, ItemStack(Material.EMERALD))
        }
        otherInventory.button(2, ItemStack(Material.EMERALD))
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            otherInventory.paginateSlot(2)
        }
    }

    @Test
    fun `parent storable initializes and saves each paginated entry separately`() {
        val player = server.addPlayer()
        val saved = mutableMapOf<Int, List<Material?>>()
        val inventory = StorablePaginatedInventory(KtInventoryPluginContext(plugin), saved)

        assertSame(inventory.pagedStorable, inventory.pagedStorables.single())
        inventory.open(player, 0)
        assertEquals(
            Material.STONE,
            player.openInventory.topInventory
                .getItem(0)
                ?.type,
        )
        player.openInventory.topInventory.setItem(0, ItemStack(Material.DIAMOND))
        player.closeInventory()

        inventory.open(player, 1)
        assertEquals(
            Material.DIRT,
            player.openInventory.topInventory
                .getItem(0)
                ?.type,
        )
        player.openInventory.topInventory.setItem(0, ItemStack(Material.GOLD_INGOT))
        player.closeInventory()

        assertEquals(listOf(Material.DIAMOND), saved.getValue(0))
        assertEquals(listOf(Material.GOLD_INGOT), saved.getValue(1))
    }

    @Test
    @Suppress("DEPRECATION")
    fun `deprecated plugin constructor still opens requested page`() {
        val player = server.addPlayer()
        val inventory = DeprecatedPaginatedInventory(plugin)

        inventory.open(player, 1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertSame(inventory, entry.paginated)
        assertEquals(1, entry.page)
    }

    @Test
    fun `open clamps page below first page`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, -1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertEquals(0, entry.page)
    }

    @Test
    fun `open clamps page above last page`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 99)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertEquals(2, entry.page)
    }

    @Test
    fun `entry navigation opens previous and next pages`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)
        (player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>).openNextPage(player)
        val next = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        next.openPreviousPage(player)
        val previous = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>

        assertEquals(2, next.page)
        assertEquals(1, previous.page)
    }

    @Test
    fun `navigation buttons open previous and next pages`() {
        val player = server.addPlayer()
        val inventory = NavigationPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 0)
        player.clickInventorySlot(8)
        val next = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        player.clickInventorySlot(7)
        val previous = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>

        assertEquals(1, next.page)
        assertEquals(0, previous.page)
    }

    @Test
    fun `button added after page creation is applied to cached pages`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 0)
        inventory.button(8, ItemStack(Material.DIAMOND))

        assertEquals(
            Material.DIAMOND,
            player.openInventory.topInventory
                .getItem(8)
                ?.type,
        )
    }

    @Test
    fun `paginated entry lookup returns null for normal inventory`() {
        val player = server.addPlayer()
        val inventory = NormalInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)

        assertNull(getTopInventoryPaginatedEntry<TestPaginatedInventory>(player))
    }

    private class TestPaginatedInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginated(context, 1) {
        override val entries =
            (0 until 5).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Test ${page + 1}/${lastPage + 1}"

        init {
            paginateSlot(0, 1)
        }
    }

    @Suppress("DEPRECATION")
    private class DeprecatedPaginatedInventory(
        plugin: org.bukkit.plugin.Plugin,
    ) : KtInventoryPaginated(plugin, 1) {
        override val entries =
            (0 until 3).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Deprecated"

        init {
            paginateSlot(0, 1)
        }
    }

    private class NormalInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        override fun title() = "Normal"
    }

    private class SlottedPaginatedInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginated(context, 1) {
        override val entries =
            listOf(
                Material.STONE,
                Material.DIRT,
                Material.DIAMOND,
                Material.GOLD_INGOT,
            ).map {
                createButton(ItemStack(it)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Slotted"

        init {
            paginateSlot(2, 4)
        }
    }

    private class NavigationPaginatedInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginated(context, 1) {
        override val entries =
            (0 until 4).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Navigation"

        init {
            paginateSlot(0, 1)
            previousPageButton(7, ItemStack(Material.ARROW))
            nextPageButton(8, ItemStack(Material.ARROW))
        }
    }

    private class StorablePaginatedInventory(
        context: KtInventoryPluginContext,
        private val saved: MutableMap<Int, List<Material?>>,
    ) : KtInventoryPaginated(context, 1) {
        override val entries =
            (0 until 2).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Storable $page"

        val pagedStorable: KtInventoryPagedStorable<AbstractKtInventoryPaginated.Entry<KtInventoryPaginated>>

        init {
            paginateSlot(8)
            pagedStorable =
                storable(
                    listOf(0),
                    initialize = {
                        listOf(
                            ItemStack(
                                if (page == 0) {
                                    Material.STONE
                                } else {
                                    Material.DIRT
                                },
                            ),
                        )
                    },
                    save = { items ->
                        saved[page] = items.map { it?.type }
                    },
                )
        }
    }
}
