package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.components.KtInventoryPagedStorable
import dev.s7a.ktinventory.util.getTopInventoryPaginated
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private typealias LazyFetchedStringMaterialEntry =
    AbstractKtInventoryPaginatedLazyFetched.Entry<KtInventoryPaginatedLazyFetched<String, Material>, String, Material>

private typealias LazyFetchedIntMaterialEntry =
    AbstractKtInventoryPaginatedLazyFetched.Entry<KtInventoryPaginatedLazyFetched<Int, Material>, Int, Material>

class AbstractKtInventoryPaginatedLazyFetchedTest {
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
    fun `open shows inventory immediately and places fetched data later`() {
        val fetchStarted = CountDownLatch(1)
        val finishFetch = CountDownLatch(1)
        val player = server.addPlayer()
        val inventory =
            OffsetLazyFetchedInventory(
                context = KtInventoryPluginContext.LazyFetchable(plugin),
                fetchStarted = fetchStarted,
                finishFetch = finishFetch,
            )

        inventory.open(player, 2)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>
        assertSame(inventory, entry.paginated)
        assertSame(inventory, getTopInventoryPaginated<OffsetLazyFetchedInventory>(player))
        assertEquals(2, entry.condition)
        assertNull(player.openInventory.topInventory.getItem(0))
        assertNull(player.openInventory.topInventory.getItem(2))

        assertTrue(fetchStarted.await(5, TimeUnit.SECONDS))
        assertNotSame(Thread.currentThread(), inventory.fetchThread)
        finishFetch.countDown()
        server.scheduler.waitAsyncTasksFinished()

        assertEquals(listOf(2), inventory.fetchedConditions)
        assertEquals(emptyList(), inventory.buttonMaterials)
        assertNull(player.openInventory.topInventory.getItem(2))

        server.scheduler.performOneTick()

        assertEquals(listOf(Material.DIAMOND, Material.GOLD_INGOT), inventory.buttonMaterials)
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
    fun `navigation buttons open previous and next lazy fetched conditions after data is loaded`() {
        val context = DeferredTaskContext(plugin)
        val player = server.addPlayer()
        val inventory = OffsetLazyFetchedInventory(context)

        inventory.open(player)
        context.runDeferredTasks()
        player.clickInventorySlot(8)
        context.runDeferredTasks()
        val next = player.openInventory.topInventory.holder as AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>
        player.clickInventorySlot(7)
        context.runDeferredTasks()
        val previous = player.openInventory.topInventory.holder as AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>

        assertEquals(2, next.condition)
        assertEquals(0, previous.condition)
        assertEquals(listOf(0, 2, 0), inventory.fetchedConditions)
    }

    @Test
    fun `loaded data is not placed when player opened another inventory`() {
        val context = DeferredTaskContext(plugin)
        val player = server.addPlayer()
        val inventory = OffsetLazyFetchedInventory(context)
        val otherInventory = OtherInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        otherInventory.open(player)
        context.runDeferredTasks()

        assertSame(otherInventory, player.openInventory.topInventory.holder)
        assertEquals(emptyList(), inventory.buttonMaterials)
    }

    @Test
    fun `stale loaded data is not placed when same player opened another lazy fetched page`() {
        val context = DeferredTaskContext(plugin)
        val player = server.addPlayer()
        val inventory = OffsetLazyFetchedInventory(context)

        inventory.open(player, 0)
        val firstEntry = player.openInventory.topInventory.holder
        inventory.open(player, 2)
        val secondEntry = player.openInventory.topInventory.holder
        context.runDeferredTasks()

        assertSame(secondEntry, player.openInventory.topInventory.holder)
        assertEquals(listOf(0, 2), inventory.fetchedConditions)
        assertEquals(listOf(Material.DIAMOND, Material.GOLD_INGOT), inventory.buttonMaterials)
        assertNull((firstEntry as AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>).page)
        assertEquals(
            Material.DIAMOND,
            player.openInventory.topInventory
                .getItem(2)
                ?.type,
        )
    }

    @Test
    fun `lazy fetched data is placed independently for concurrently opened players`() {
        val context = DeferredTaskContext(plugin)
        val firstPlayer = server.addPlayer()
        val secondPlayer = server.addPlayer()
        val inventory = OffsetLazyFetchedInventory(context)

        inventory.open(firstPlayer, 0)
        inventory.open(secondPlayer, 2)
        context.runDeferredTasks()

        assertEquals(listOf(0, 2), inventory.fetchedConditions)
        assertEquals(
            Material.STONE,
            firstPlayer.openInventory.topInventory
                .getItem(2)
                ?.type,
        )
        assertEquals(
            Material.DIAMOND,
            secondPlayer.openInventory.topInventory
                .getItem(2)
                ?.type,
        )
    }

    @Test
    fun `open fetches lazy entries by cursor condition`() {
        val context = DeferredTaskContext(plugin)
        val player = server.addPlayer()
        val inventory = CursorLazyFetchedInventory(context)

        inventory.open(player)
        context.runDeferredTasks()
        player.clickInventorySlot(8)
        context.runDeferredTasks()

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>
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

        player.clickInventorySlot(7)
        context.runDeferredTasks()

        val previous = player.openInventory.topInventory.holder as AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>
        assertEquals("start", previous.condition)
        assertEquals(listOf("start", "cursor-2", "start"), inventory.fetchedConditions)
    }

    @Test
    fun `fixed buttons cannot share lazy fetched pagination slots`() {
        val context = DeferredTaskContext(plugin)
        val inventory = OffsetLazyFetchedInventory(context)
        val otherInventory = OffsetLazyFetchedWithoutSlotsInventory(context)

        kotlin.test.assertFailsWith<IllegalArgumentException> {
            inventory.button(2, ItemStack(Material.EMERALD))
        }
        otherInventory.button(2, ItemStack(Material.EMERALD))
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            otherInventory.paginateSlot(2)
        }
    }

    @Test
    fun `parent storable initializes and saves each lazy fetched entry separately`() {
        val context = DeferredTaskContext(plugin)
        val player = server.addPlayer()
        val saved = mutableMapOf<Int, List<Material?>>()
        val inventory = StorableLazyFetchedInventory(context, saved)

        assertSame(inventory.pagedStorable, inventory.pagedStorables.single())
        inventory.open(player, 0)
        context.runDeferredTasks()
        assertEquals(
            Material.STONE,
            player.openInventory.topInventory
                .getItem(0)
                ?.type,
        )
        player.openInventory.topInventory.setItem(0, ItemStack(Material.DIAMOND))
        player.closeInventory()

        inventory.open(player, 1)
        context.runDeferredTasks()
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

    private class OffsetLazyFetchedInventory(
        context: KtInventoryPluginContext.LazyFetchable,
        private val fetchStarted: CountDownLatch? = null,
        private val finishFetch: CountDownLatch? = null,
    ) : KtInventoryPaginatedLazyFetched<Int, Material>(context, 1) {
        private val materials =
            listOf(
                Material.STONE,
                Material.DIRT,
                Material.DIAMOND,
                Material.GOLD_INGOT,
            )
        val fetchedConditions = mutableListOf<Int>()
        val buttonMaterials = mutableListOf<Material>()
        var fetchThread: Thread? = null
            private set

        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): AbstractKtInventoryPaginatedFetched.Page<Int, Material> {
            fetchThread = Thread.currentThread()
            fetchStarted?.countDown()
            check(finishFetch?.await(5, TimeUnit.SECONDS) ?: true)
            fetchedConditions += condition
            val nextCondition = condition + limit
            val previousCondition = condition - limit
            return AbstractKtInventoryPaginatedFetched.Page(
                entries = materials.drop(condition).take(limit),
                previousCondition = previousCondition.takeIf { it >= 0 },
                nextCondition = nextCondition.takeIf { it < materials.size },
            )
        }

        override fun createButton(
            data: Material,
        ): KtInventoryButton<AbstractKtInventoryPaginatedLazyFetched.Entry<KtInventoryPaginatedLazyFetched<Int, Material>, Int, Material>> {
            buttonMaterials += data
            return createButton(ItemStack(data)) {}
        }

        override fun title(condition: Int) = "Lazy $condition"

        init {
            paginateSlot(2, 4)
            previousPageButton(7, ItemStack(Material.ARROW))
            nextPageButton(8, ItemStack(Material.ARROW))
        }
    }

    private class OffsetLazyFetchedWithoutSlotsInventory(
        context: KtInventoryPluginContext.LazyFetchable,
    ) : KtInventoryPaginatedLazyFetched<Int, Material>(context, 1) {
        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): AbstractKtInventoryPaginatedFetched.Page<Int, Material> = AbstractKtInventoryPaginatedFetched.Page(emptyList())

        override fun createButton(
            data: Material,
        ): KtInventoryButton<AbstractKtInventoryPaginatedLazyFetched.Entry<KtInventoryPaginatedLazyFetched<Int, Material>, Int, Material>> =
            createButton(ItemStack(data)) {}

        override fun title(condition: Int) = "Lazy $condition"
    }

    private class CursorLazyFetchedInventory(
        context: KtInventoryPluginContext.LazyFetchable,
    ) : KtInventoryPaginatedLazyFetched<String, Material>(context, 1) {
        val fetchedConditions = mutableListOf<String>()

        override val initialCondition = "start"

        override fun fetch(
            condition: String,
            limit: Int,
        ): AbstractKtInventoryPaginatedFetched.Page<String, Material> {
            fetchedConditions += condition
            return when (condition) {
                "start" -> {
                    AbstractKtInventoryPaginatedFetched.Page(
                        entries = listOf(Material.STONE, Material.DIRT).take(limit),
                        nextCondition = "cursor-2",
                    )
                }

                else -> {
                    AbstractKtInventoryPaginatedFetched.Page(
                        entries = listOf(Material.DIAMOND, Material.GOLD_INGOT).take(limit),
                        previousCondition = "start",
                    )
                }
            }
        }

        override fun createButton(data: Material): KtInventoryButton<LazyFetchedStringMaterialEntry> = createButton(ItemStack(data)) {}

        override fun title(condition: String) = condition

        init {
            paginateSlot(2, 4)
            previousPageButton(7, ItemStack(Material.ARROW))
            nextPageButton(8, ItemStack(Material.ARROW))
        }
    }

    private class StorableLazyFetchedInventory(
        context: KtInventoryPluginContext.LazyFetchable,
        private val saved: MutableMap<Int, List<Material?>>,
    ) : KtInventoryPaginatedLazyFetched<Int, Material>(context, 1) {
        override val initialCondition = 0

        override fun fetch(
            condition: Int,
            limit: Int,
        ): AbstractKtInventoryPaginatedFetched.Page<Int, Material> =
            AbstractKtInventoryPaginatedFetched.Page(
                entries = listOf(Material.STONE),
                previousCondition = (condition - 1).takeIf { it >= 0 },
                nextCondition = (condition + 1).takeIf { it < 2 },
            )

        override fun createButton(data: Material): KtInventoryButton<LazyFetchedIntMaterialEntry> = createButton(ItemStack(data)) {}

        override fun title(condition: Int) = "Storable $condition"

        val pagedStorable: KtInventoryPagedStorable<LazyFetchedIntMaterialEntry>

        init {
            paginateSlot(8)
            pagedStorable =
                storable(
                    listOf(0),
                    initialize = {
                        listOf(
                            ItemStack(
                                if (condition == 0) {
                                    Material.STONE
                                } else {
                                    Material.DIRT
                                },
                            ),
                        )
                    },
                    save = { items ->
                        saved[condition] = items.map { it?.type }
                    },
                )
        }
    }

    private class OtherInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        override fun title() = "Other"
    }

    private class DeferredTaskContext(
        protected val plugin: Plugin,
    ) : KtInventoryPluginContext.LazyFetchable {
        var syncTaskCount = 0
            private set
        var asyncTaskCount = 0
            private set
        private val syncTasks = mutableListOf<() -> Unit>()
        private val asyncTasks = mutableListOf<() -> Unit>()

        override val handlerId = KtInventoryHandlerId.of(plugin)

        override fun registerEvents(listener: Listener) {
            plugin.server.pluginManager.registerEvents(listener, plugin)
        }

        override fun runTask(block: () -> Unit) =
            plugin.server.scheduler.runTask(plugin, Runnable {}).also {
                syncTaskCount += 1
                syncTasks += block
            }

        override fun runTaskAsync(block: () -> Unit) =
            plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {}).also {
                asyncTaskCount += 1
                asyncTasks += block
            }

        fun runDeferredTasks() {
            while (asyncTasks.isNotEmpty() || syncTasks.isNotEmpty()) {
                if (asyncTasks.isNotEmpty()) {
                    runNextAsyncTask()
                } else {
                    runNextSyncTask()
                }
            }
        }

        fun runNextAsyncTask() {
            asyncTasks.removeFirst().invoke()
        }

        fun runNextSyncTask() {
            syncTasks.removeFirst().invoke()
        }
    }
}
