package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventoryPaginatedFetched
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedLazyFetched
import dev.s7a.ktinventory.HasParentInventory
import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPaginated
import dev.s7a.ktinventory.KtInventoryPaginatedFetched
import dev.s7a.ktinventory.KtInventoryPaginatedLazyFetched
import dev.s7a.ktinventory.KtInventoryPaginatedSequence
import dev.s7a.ktinventory.KtInventoryPluginContext
import dev.s7a.ktinventory.ParentInventory
import dev.s7a.ktinventory.components.KtInventoryButton
import org.bukkit.Material
import org.bukkit.entity.Player
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

private typealias LazyFetchedTestEntry =
    AbstractKtInventoryPaginatedLazyFetched.Entry<KtInventoryPaginatedLazyFetched<Int, Material>, Int, Material>

private typealias FetchedTestEntry =
    AbstractKtInventoryPaginatedFetched.Entry<KtInventoryPaginatedFetched<Int>, Int>

class GetViewersTest {
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
    fun `getViewers returns players viewing matching top inventories`() {
        val player = server.addPlayer()
        val other = server.addPlayer()
        val inventory = NormalInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        OtherInventory(KtInventoryPluginContext(plugin)).open(other)

        assertEquals(mapOf<Player, NormalInventory>(player to inventory), getViewers<NormalInventory>())
        assertEquals(emptyMap(), getViewers<ParentInventory>())
    }

    @Test
    fun `getViewers returns paginated owners and getViewersPaginatedEntry returns entries`() {
        val player = server.addPlayer()
        val inventory = PaginatedParentInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

        val entry = getTopInventoryPaginatedEntry<PaginatedParentInventory>(player)
        assertSame(inventory, getViewers<PaginatedParentInventory>().getValue(player))
        assertSame(entry, getViewersPaginatedEntry<PaginatedParentInventory>().getValue(player))
    }

    @Test
    fun `getViewersPaginatedSequenceEntry returns sequence entries`() {
        val player = server.addPlayer()
        val inventory = SequenceParentInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

        val entry = getTopInventoryPaginatedSequenceEntry<SequenceParentInventory>(player)
        assertSame(inventory, getViewers<SequenceParentInventory>().getValue(player))
        assertSame(entry, getViewersPaginatedSequenceEntry<SequenceParentInventory>().getValue(player))
    }

    @Test
    fun `getViewersPaginatedFetchedEntry returns fetched entries`() {
        val player = server.addPlayer()
        val inventory = FetchedParentInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)

        val entry = getTopInventoryPaginatedFetchedEntry<FetchedParentInventory>(player)
        assertSame(inventory, getViewers<FetchedParentInventory>().getValue(player))
        assertSame(entry, getViewersPaginatedFetchedEntry<FetchedParentInventory>().getValue(player))
    }

    @Test
    fun `getViewersPaginatedLazyFetchedEntry returns lazy fetched entries`() {
        val player = server.addPlayer()
        val inventory = LazyFetchedParentInventory(KtInventoryPluginContext.LazyFetchable(plugin))

        inventory.open(player)

        val entry = getTopInventoryPaginatedLazyFetchedEntry<LazyFetchedParentInventory>(player)
        assertSame(inventory, getViewers<LazyFetchedParentInventory>().getValue(player))
        assertSame(entry, getViewersPaginatedLazyFetchedEntry<LazyFetchedParentInventory>().getValue(player))
    }

    @Test
    fun `top inventory lookups return null for mismatched holder types`() {
        val player = server.addPlayer()
        val normal = NormalInventory(KtInventoryPluginContext(plugin))
        val paginated = PaginatedParentInventory(KtInventoryPluginContext(plugin))
        val sequence = SequenceParentInventory(KtInventoryPluginContext(plugin))

        normal.open(player)
        assertNull(getTopInventory<OtherInventory>(player))
        assertNull(getTopInventory<PaginatedParentInventory>(player))
        assertNull(getTopInventoryPaginatedEntry<PaginatedParentInventory>(player))
        assertNull(getTopInventoryPaginatedSequenceEntry<SequenceParentInventory>(player))
        assertNull(getTopInventoryPaginatedFetchedEntry<FetchedParentInventory>(player))
        assertNull(getTopInventoryPaginatedLazyFetchedEntry<LazyFetchedParentInventory>(player))

        paginated.open(player)
        assertSame(paginated, getTopInventory<PaginatedParentInventory>(player))
        assertNull(getTopInventoryPaginatedSequenceEntry<SequenceParentInventory>(player))
        assertNull(getTopInventoryPaginatedFetchedEntry<FetchedParentInventory>(player))
        assertNull(getTopInventoryPaginatedLazyFetchedEntry<LazyFetchedParentInventory>(player))

        sequence.open(player)
        assertNull(getTopInventoryPaginatedEntry<PaginatedParentInventory>(player))
        assertNull(getTopInventoryPaginatedFetchedEntry<FetchedParentInventory>(player))
        assertNull(getTopInventoryPaginatedLazyFetchedEntry<LazyFetchedParentInventory>(player))
    }

    @Test
    fun `top inventory lookup returns null after inventory is closed`() {
        val player = server.addPlayer()
        val normal = NormalInventory(KtInventoryPluginContext(plugin))

        normal.open(player)
        player.closeInventory()

        assertNull(getTopInventory<NormalInventory>(player))
        assertNull(getTopInventory<PaginatedParentInventory>(player))
    }

    @Test
    fun `getViewersDeeply finds direct parent child parent and paginated owners`() {
        val parentPlayer = server.addPlayer()
        val childPlayer = server.addPlayer()
        val paginatedPlayer = server.addPlayer()
        val sequencePlayer = server.addPlayer()
        val paginatedChildPlayer = server.addPlayer()
        val sequenceChildPlayer = server.addPlayer()
        val parent = ParentNormalInventory(KtInventoryPluginContext(plugin))
        val child = ChildInventory(KtInventoryPluginContext(plugin), parent)
        val paginated = PaginatedParentInventory(KtInventoryPluginContext(plugin))
        val sequence = SequenceParentInventory(KtInventoryPluginContext(plugin))
        val paginatedChild = PaginatedChildInventory(KtInventoryPluginContext(plugin), parent)
        val sequenceChild = SequenceChildInventory(KtInventoryPluginContext(plugin), parent)

        parent.open(parentPlayer)
        child.open(childPlayer)
        paginated.open(paginatedPlayer)
        sequence.open(sequencePlayer)
        paginatedChild.open(paginatedChildPlayer)
        sequenceChild.open(sequenceChildPlayer)

        val expected: Map<Player, ParentInventory> =
            mapOf(
                parentPlayer to parent,
                childPlayer to parent,
                paginatedPlayer to paginated,
                sequencePlayer to sequence,
                paginatedChildPlayer to parent,
                sequenceChildPlayer to parent,
            )

        assertEquals(expected, getViewersDeeply<ParentInventory>())
    }

    @Test
    fun `getViewersDeeply ignores child inventories whose parent has a different type`() {
        val childPlayer = server.addPlayer()
        val paginatedChildPlayer = server.addPlayer()
        val sequenceChildPlayer = server.addPlayer()
        val otherParent = OtherParentInventory(KtInventoryPluginContext(plugin))
        val child = ChildWithOtherParentInventory(KtInventoryPluginContext(plugin), otherParent)
        val paginatedChild = PaginatedChildWithOtherParentInventory(KtInventoryPluginContext(plugin), otherParent)
        val sequenceChild = SequenceChildWithOtherParentInventory(KtInventoryPluginContext(plugin), otherParent)

        child.open(childPlayer)
        paginatedChild.open(paginatedChildPlayer)
        sequenceChild.open(sequenceChildPlayer)

        assertEquals(emptyMap(), getViewersDeeply<ParentNormalInventory>())
    }

    @Test
    @Suppress("DEPRECATION")
    fun `deprecated viewer and open inventory aliases delegate to replacements`() {
        val player = server.addPlayer()
        val normal = NormalInventory(KtInventoryPluginContext(plugin))
        val paginated = PaginatedParentInventory(KtInventoryPluginContext(plugin))

        normal.open(player)
        assertSame(getTopInventory<NormalInventory>(player), getOpenInventory<NormalInventory>(player))
        assertSame(getTopInventory(NormalInventory::class, player), getOpenInventory(NormalInventory::class, player))
        assertEquals(getViewers<NormalInventory>(), getAllViewers<NormalInventory>())
        assertEquals(getViewers(NormalInventory::class), getAllViewers(NormalInventory::class))

        paginated.open(player)
        assertSame(
            getTopInventoryPaginatedEntry<PaginatedParentInventory>(player),
            getOpenInventoryPaginated<PaginatedParentInventory>(player),
        )
        assertSame(
            getTopInventoryPaginatedEntry(PaginatedParentInventory::class, player),
            getOpenInventoryPaginated(PaginatedParentInventory::class, player),
        )
        assertEquals(getViewersPaginatedEntry<PaginatedParentInventory>(), getAllViewersPaginated<PaginatedParentInventory>())
        assertEquals(
            getViewersPaginatedEntry(PaginatedParentInventory::class),
            getAllViewersPaginated(PaginatedParentInventory::class),
        )
    }

    private class NormalInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        override fun title() = "Normal"
    }

    private class OtherInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        override fun title() = "Other"
    }

    private class ParentNormalInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1),
        ParentInventory {
        override fun title() = "Parent"
    }

    private class ChildInventory(
        context: KtInventoryPluginContext,
        override val parentInventory: ParentNormalInventory,
    ) : KtInventory(context, 1),
        HasParentInventory<ParentNormalInventory> {
        override fun title() = "Child"
    }

    private class OtherParentInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1),
        ParentInventory {
        override fun title() = "Other Parent"
    }

    private class ChildWithOtherParentInventory(
        context: KtInventoryPluginContext,
        override val parentInventory: OtherParentInventory,
    ) : KtInventory(context, 1),
        HasParentInventory<OtherParentInventory> {
        override fun title() = "Child With Other Parent"
    }

    private class PaginatedParentInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginated(context, 1),
        ParentInventory {
        override val entries =
            (0 until 4).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Paginated"

        init {
            paginateSlot(0, 1)
        }
    }

    private class PaginatedChildInventory(
        context: KtInventoryPluginContext,
        override val parentInventory: ParentNormalInventory,
    ) : KtInventoryPaginated(context, 1),
        HasParentInventory<ParentNormalInventory> {
        override val entries =
            (0 until 2).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Paginated Child"

        init {
            paginateSlot(0, 1)
        }
    }

    private class PaginatedChildWithOtherParentInventory(
        context: KtInventoryPluginContext,
        override val parentInventory: OtherParentInventory,
    ) : KtInventoryPaginated(context, 1),
        HasParentInventory<OtherParentInventory> {
        override val entries =
            (0 until 2).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Paginated Child With Other Parent"

        init {
            paginateSlot(0, 1)
        }
    }

    private class SequenceParentInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginatedSequence(context, 1),
        ParentInventory {
        override val entries
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = "Sequence"

        init {
            paginateSlot(0, 1)
        }
    }

    private class SequenceChildInventory(
        context: KtInventoryPluginContext,
        override val parentInventory: ParentNormalInventory,
    ) : KtInventoryPaginatedSequence(context, 1),
        HasParentInventory<ParentNormalInventory> {
        override val entries
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = "Sequence Child"

        init {
            paginateSlot(0, 1)
        }
    }

    private class SequenceChildWithOtherParentInventory(
        context: KtInventoryPluginContext,
        override val parentInventory: OtherParentInventory,
    ) : KtInventoryPaginatedSequence(context, 1),
        HasParentInventory<OtherParentInventory> {
        override val entries
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = "Sequence Child With Other Parent"

        init {
            paginateSlot(0, 1)
        }
    }

    private class FetchedParentInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginatedFetched<Int>(context, 1),
        ParentInventory {
        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): Page<Int, KtInventoryButton<FetchedTestEntry>> =
            Page(
                entries = listOf(createButton(ItemStack(Material.STONE)) {}),
                nextCondition = condition + 1,
            )

        override fun title(condition: Int) = "Fetched $condition"

        init {
            paginateSlot(0, 1)
        }
    }

    private class LazyFetchedParentInventory(
        context: KtInventoryPluginContext.LazyFetchable,
    ) : KtInventoryPaginatedLazyFetched<Int, Material>(context, 1),
        ParentInventory {
        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): AbstractKtInventoryPaginatedFetched.Page<Int, Material> =
            AbstractKtInventoryPaginatedFetched.Page(
                entries = listOf(Material.STONE),
                nextCondition = condition + 1,
            )

        override fun createButton(data: Material): KtInventoryButton<LazyFetchedTestEntry> = createButton(ItemStack(data)) {}

        override fun title(condition: Int) = "LazyFetched $condition"

        init {
            paginateSlot(0, 1)
        }
    }
}
