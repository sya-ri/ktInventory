package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RefreshableInventoryTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock
    private lateinit var context: KtInventoryPluginContext

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        context = KtInventoryPluginContext(plugin)
        RefreshableNormalInventory.resetRefresh()
        RefreshablePaginatedInventory.resetRefresh()
        RefreshableSequenceInventory.resetRefresh()
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `normal refresh replaces current inventory when predicate matches`() {
        val player = server.addPlayer()
        val original = RefreshableNormalInventory(context, "old")

        original.open(player)
        val refreshed = RefreshableNormalInventory.refresh(player) { it.marker == "old" }

        val current = player.openInventory.topInventory.holder as RefreshableNormalInventory
        assertTrue(refreshed)
        assertNotSame(original, current)
        assertEquals("new", current.marker)
    }

    @Test
    fun `normal refresh returns false when top inventory does not match`() {
        val player = server.addPlayer()

        OtherInventory(context).open(player)

        assertFalse(RefreshableNormalInventory.refresh(player))
    }

    @Test
    fun `normal refresh closes inventory when createNew returns null`() {
        val player = server.addPlayer()
        val original = RefreshableNormalInventory(context, "old")
        RefreshableNormalInventory.shouldCreate = false

        original.open(player)
        val refreshed = RefreshableNormalInventory.refresh(player)

        assertTrue(refreshed)
        assertEquals(1, original.closeCount)
    }

    @Test
    fun `paginated refresh opens first page by default`() {
        val player = server.addPlayer()
        val original = RefreshablePaginatedInventory(context, "old")

        original.open(player, 1)
        RefreshablePaginatedInventory.refresh(player)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        val current = entry.paginated as RefreshablePaginatedInventory
        assertEquals(0, entry.page)
        assertEquals("new", current.marker)
    }

    @Test
    fun `paginated refresh can keep current page`() {
        val player = server.addPlayer()
        val original = RefreshablePaginatedInventory(context, "old")

        original.open(player, 1)
        RefreshablePaginatedInventory.refresh(
            player,
            AbstractKtInventoryPaginated.Refreshable.RefreshBehavior.Keep,
        )

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        val current = entry.paginated as RefreshablePaginatedInventory
        assertEquals(1, entry.page)
        assertEquals("new", current.marker)
    }

    @Test
    fun `paginated refresh returns false when predicate does not match`() {
        val player = server.addPlayer()
        val original = RefreshablePaginatedInventory(context, "old")

        original.open(player, 1)

        assertFalse(RefreshablePaginatedInventory.refresh(player) { false })
        assertSame(original, (player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>).paginated)
    }

    @Test
    fun `paginated refresh closes inventory when createNew returns null`() {
        val player = server.addPlayer()
        val original = RefreshablePaginatedInventory(context, "old")
        RefreshablePaginatedInventory.shouldCreate = false

        original.open(player, 1)
        val refreshed = RefreshablePaginatedInventory.refresh(player)

        assertTrue(refreshed)
        assertEquals(1, original.closeCount)
    }

    @Test
    fun `sequence refresh opens first page by default`() {
        val player = server.addPlayer()
        val original = RefreshableSequenceInventory(context, "old")

        original.open(player, 2)
        RefreshableSequenceInventory.refresh(player)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        val current = entry.paginated as RefreshableSequenceInventory
        assertEquals(0, entry.page)
        assertEquals("new", current.marker)
    }

    @Test
    fun `sequence refresh returns false when predicate does not match`() {
        val player = server.addPlayer()
        val original = RefreshableSequenceInventory(context, "old")

        original.open(player, 1)

        assertFalse(RefreshableSequenceInventory.refresh(player) { false })
        assertSame(original, (player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>).paginated)
    }

    @Test
    fun `sequence refresh closes inventory when createNew returns null`() {
        val player = server.addPlayer()
        val original = RefreshableSequenceInventory(context, "old")
        RefreshableSequenceInventory.shouldCreate = false

        original.open(player, 1)
        val refreshed = RefreshableSequenceInventory.refresh(player)

        assertTrue(refreshed)
        assertEquals(1, original.closeCount)
    }

    @Test
    fun `sequence refresh can keep current page and refresh all viewers`() {
        val player = server.addPlayer()
        val original = RefreshableSequenceInventory(context, "old")

        original.open(player, 2)
        RefreshableSequenceInventory.refreshAll(AbstractKtInventorySequence.Refreshable.RefreshBehavior.Keep)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        val current = entry.paginated as RefreshableSequenceInventory
        assertEquals(2, entry.page)
        assertEquals("new", current.marker)
    }

    @Test
    fun `refreshAll only refreshes matching viewers`() {
        val matchingPlayer = server.addPlayer()
        val skippedPlayer = server.addPlayer()
        RefreshableNormalInventory(context, "match").open(matchingPlayer)
        val skipped = RefreshableNormalInventory(context, "skip")
        skipped.open(skippedPlayer)

        RefreshableNormalInventory.refreshAll { _, inventory -> inventory.marker == "match" }

        assertEquals("new", (matchingPlayer.openInventory.topInventory.holder as RefreshableNormalInventory).marker)
        assertSame(skipped, skippedPlayer.openInventory.topInventory.holder)
    }

    private class RefreshableNormalInventory(
        private val context: KtInventoryPluginContext,
        val marker: String,
    ) : KtInventory(context, 1) {
        companion object : AbstractKtInventory.Refreshable<RefreshableNormalInventory>(RefreshableNormalInventory::class) {
            var nextMarker = "new"
            var shouldCreate = true

            fun resetRefresh() {
                nextMarker = "new"
                shouldCreate = true
            }

            override fun createNew(
                player: org.bukkit.entity.HumanEntity,
                inventory: RefreshableNormalInventory,
            ) = if (shouldCreate) RefreshableNormalInventory(inventory.context, nextMarker) else null
        }

        var closeCount = 0
            private set

        override fun title() = marker

        override fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
            closeCount += 1
        }
    }

    private class OtherInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        override fun title() = "other"
    }

    private class RefreshablePaginatedInventory(
        private val context: KtInventoryPluginContext,
        val marker: String,
    ) : KtInventoryPaginated(context, 1) {
        companion object : KtInventoryPaginated.Refreshable<RefreshablePaginatedInventory>(RefreshablePaginatedInventory::class) {
            var nextMarker = "new"
            var shouldCreate = true

            fun resetRefresh() {
                nextMarker = "new"
                shouldCreate = true
            }

            override fun createNew(
                player: org.bukkit.entity.HumanEntity,
                inventory: AbstractKtInventoryPaginated.Entry<RefreshablePaginatedInventory>,
            ) = if (shouldCreate) RefreshablePaginatedInventory(inventory.paginated.context, nextMarker) else null
        }

        var closeCount = 0
            private set

        override val entries =
            (0 until 4).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = marker

        override fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
            closeCount += 1
        }

        init {
            paginateSlot(0, 1)
        }
    }

    private class RefreshableSequenceInventory(
        private val context: KtInventoryPluginContext,
        val marker: String,
    ) : KtInventorySequence(context, 1) {
        companion object : KtInventorySequence.Refreshable<RefreshableSequenceInventory>(RefreshableSequenceInventory::class) {
            var nextMarker = "new"
            var shouldCreate = true

            fun resetRefresh() {
                nextMarker = "new"
                shouldCreate = true
            }

            override fun createNew(
                player: org.bukkit.entity.HumanEntity,
                inventory: AbstractKtInventorySequence.Entry<RefreshableSequenceInventory>,
            ) = if (shouldCreate) RefreshableSequenceInventory(inventory.paginated.context, nextMarker) else null
        }

        var closeCount = 0
            private set

        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequence>>>
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = marker

        override fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
            closeCount += 1
        }

        init {
            paginateSlot(0, 1)
        }
    }
}
