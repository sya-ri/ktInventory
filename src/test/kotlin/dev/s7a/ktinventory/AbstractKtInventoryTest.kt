package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryStorable
import dev.s7a.ktinventory.util.getTopInventory
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.DragType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AbstractKtInventoryTest {
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
    fun `open shows inventory to player`() {
        val player = server.addPlayer()
        val inventory = TestInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)

        assertSame(inventory, player.openInventory.topInventory.holder)
        assertSame(inventory, getTopInventory<TestInventory>(player))
        assertEquals(
            Material.STONE,
            player.openInventory.topInventory
                .getItem(0)
                ?.type,
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun `deprecated plugin constructor and null color translation constructor still open`() {
        val player = server.addPlayer()
        val deprecated = DeprecatedConstructorInventory(plugin)
        val noColor = NoColorInventory(KtInventoryPluginContext(plugin))

        deprecated.open(player)
        assertSame(deprecated, player.openInventory.topInventory.holder)

        noColor.open(player)
        assertSame(noColor, player.openInventory.topInventory.holder)
    }

    @Test
    fun `plugin contexts share handler id per plugin instance`() {
        val otherPlugin = MockBukkit.createMockPlugin()

        assertSame(KtInventoryPluginContext(plugin).handlerId, KtInventoryPluginContext(plugin).handlerId)
        assertTrue(KtInventoryPluginContext(plugin).handlerId !== KtInventoryPluginContext(otherPlugin).handlerId)
    }

    @Test
    fun `line must be between one and six`() {
        assertFailsWith<IllegalArgumentException> {
            TestInventory(KtInventoryPluginContext(plugin), 0)
        }
        assertFailsWith<IllegalArgumentException> {
            TestInventory(KtInventoryPluginContext(plugin), 7)
        }
    }

    @Test
    fun `button rejects slots outside inventory size`() {
        val inventory = TestInventory(KtInventoryPluginContext(plugin))

        assertFailsWith<IllegalArgumentException> {
            inventory.button(9, ItemStack(Material.STONE))
        }
    }

    @Test
    fun `button click and inventory callbacks are invoked`() {
        val player = server.addPlayer()
        val inventory = CallbackInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        val event = player.clickInventorySlot(0)

        assertTrue(event.isCancelled)
        assertEquals(1, inventory.openCount)
        assertEquals(1, inventory.buttonClickCount)
        assertEquals(1, inventory.topClickCount)
        assertEquals(0, inventory.bottomClickCount)
        assertSame(player, inventory.lastButtonPlayer)
        assertEquals(0, inventory.lastButtonSlot)
        assertEquals(ClickType.LEFT, inventory.lastButtonClick)
        assertEquals(InventoryAction.UNKNOWN, inventory.lastButtonAction)
    }

    @Test
    fun `button join invokes original handler before joined handler`() {
        val player = server.addPlayer()
        val inventory = JoinedButtonInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        player.clickInventorySlot(0)

        assertEquals(listOf("original", "joined"), inventory.clicks)
    }

    @Test
    fun `bottom inventory click delegates to bottom callback`() {
        val player = server.addPlayer()
        val inventory = CallbackInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        player.clickInventorySlot(inventory.size)

        assertEquals(0, inventory.topClickCount)
        assertEquals(1, inventory.bottomClickCount)
    }

    @Test
    fun `storable allows click and saves managed slots on close`() {
        val player = server.addPlayer()
        val saved = mutableListOf<List<ItemStack?>>()
        val inventory = StorableInventory(KtInventoryPluginContext(plugin), saved)

        inventory.open(player)
        val event = player.clickInventorySlot(0)
        player.closeInventory()

        assertFalse(event.isCancelled)
        assertEquals(1, inventory.preClickCount)
        assertEquals(1, inventory.clickCount)
        assertSame(player, inventory.lastClickPlayer)
        assertEquals(0, inventory.lastClickSlot)
        assertEquals(ClickType.LEFT, inventory.lastClickType)
        assertEquals(InventoryAction.UNKNOWN, inventory.lastClickAction)
        assertEquals(listOf(Material.DIRT, Material.STONE), saved.single().map { it?.type })
    }

    @Test
    fun `button overloads add buttons to all requested slots`() {
        val inventory = TestInventory(KtInventoryPluginContext(plugin))
        val shared = inventory.createButton(ItemStack(Material.GOLD_INGOT)) {}

        inventory.button(1, 2, itemStack = ItemStack(Material.DIRT))
        inventory.button(listOf(3, 4), ItemStack(Material.STONE))
        inventory.button(5, 6, item = shared)
        inventory.button(listOf(7, 8), shared)

        assertEquals(Material.DIRT, inventory.getItem(1)?.type)
        assertEquals(Material.DIRT, inventory.getItem(2)?.type)
        assertEquals(Material.STONE, inventory.getItem(3)?.type)
        assertEquals(Material.STONE, inventory.getItem(4)?.type)
        assertEquals(Material.GOLD_INGOT, inventory.getItem(5)?.type)
        assertEquals(Material.GOLD_INGOT, inventory.getItem(6)?.type)
        assertEquals(Material.GOLD_INGOT, inventory.getItem(7)?.type)
        assertEquals(Material.GOLD_INGOT, inventory.getItem(8)?.type)
    }

    @Test
    fun `storable exposes contains update clear get and lookup helpers`() {
        val inventory = EmptyInventory(KtInventoryPluginContext(plugin))
        val storable =
            inventory.storable(
                0 until 9,
                initialize = { listOf(ItemStack(Material.STONE)) },
            )

        val remaining = storable.update(List(10) { ItemStack(Material.DIRT) })
        storable.clear()

        assertTrue(storable.contains(0))
        assertEquals(1, remaining.size)
        assertEquals(1, inventory.storables.size)
        assertEquals(listOf(storable), inventory.getStorables(0))
        assertEquals(listOf(storable), inventory.getStorables(listOf(0, 8)))
        assertEquals(List<ItemStack?>(9) { null }, storable.get())
    }

    @Test
    fun `fixed buttons cannot share storable slots`() {
        val inventory = EmptyInventory(KtInventoryPluginContext(plugin))
        val otherInventory = EmptyInventory(KtInventoryPluginContext(plugin))

        inventory.button(0, ItemStack(Material.EMERALD))
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            inventory.storable(0)
        }
        otherInventory.storable(listOf(0))
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            otherInventory.button(0, ItemStack(Material.EMERALD))
        }
    }

    @Test
    fun `storable without slot arguments manages all inventory slots and treats air as empty`() {
        val inventory = EmptyInventory(KtInventoryPluginContext(plugin))

        inventory.storable(
            initialize = {
                listOf(
                    ItemStack(Material.AIR),
                    ItemStack(Material.STONE),
                )
            },
        )

        val storable = inventory.storables.single()
        assertEquals((0 until inventory.size).toList(), storable.slots)
        assertNull(storable.get()[0])
        assertEquals(Material.STONE, storable.get()[1]?.type)
    }

    @Test
    fun `storable drag callbacks are invoked for managed slots`() {
        val player = server.addPlayer()
        val inventory = DragInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        val event =
            InventoryDragEvent(
                player.openInventory,
                ItemStack(Material.STONE),
                ItemStack(Material.DIRT),
                false,
                mapOf(0 to ItemStack(Material.STONE)),
            )
        server.pluginManager.callEvent(event)

        assertFalse(event.isCancelled)
        assertEquals(1, inventory.preDragCount)
        assertEquals(1, inventory.dragCount)
        assertSame(player, inventory.lastDragPlayer)
        assertEquals(setOf(0), inventory.lastDragSlots)
        assertEquals(setOf(0), inventory.lastDragRawSlots)
        assertEquals(DragType.EVEN, inventory.lastDragType)
        assertEquals(Material.STONE, inventory.lastDragCursor?.type)
        assertEquals(Material.DIRT, inventory.lastDragOldCursor?.type)
        assertEquals(Material.STONE, inventory.lastDragNewItems?.get(0)?.type)
    }

    @Test
    fun `storable denied click is cancelled and skips click callback`() {
        val player = server.addPlayer()
        val inventory = DenyClickInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        val event = player.clickInventorySlot(0)

        assertTrue(event.isCancelled)
        assertEquals(1, inventory.preClickCount)
        assertEquals(0, inventory.clickCount)
    }

    @Test
    fun `clicking unmanaged top slot is cancelled without storable callbacks`() {
        val player = server.addPlayer()
        val inventory = UnmanagedStorableInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        val event = player.clickInventorySlot(0)

        assertTrue(event.isCancelled)
        assertEquals(0, inventory.preClickCount)
        assertEquals(0, inventory.clickCount)
    }

    @Test
    fun `storable denied drag is cancelled and skips drag callback`() {
        val player = server.addPlayer()
        val inventory = DenyDragInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        val event =
            InventoryDragEvent(
                player.openInventory,
                ItemStack(Material.STONE),
                ItemStack(Material.DIRT),
                false,
                mapOf(0 to ItemStack(Material.STONE)),
            )
        server.pluginManager.callEvent(event)

        assertTrue(event.isCancelled)
        assertEquals(1, inventory.preDragCount)
        assertEquals(0, inventory.dragCount)
    }

    @Test
    fun `dragging unmanaged top slot is cancelled without storable callbacks`() {
        val player = server.addPlayer()
        val inventory = UnmanagedStorableInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        val event =
            InventoryDragEvent(
                player.openInventory,
                ItemStack(Material.STONE),
                ItemStack(Material.DIRT),
                false,
                mapOf(0 to ItemStack(Material.STONE)),
            )
        server.pluginManager.callEvent(event)

        assertTrue(event.isCancelled)
        assertEquals(0, inventory.preDragCount)
        assertEquals(0, inventory.dragCount)
    }

    @Test
    fun `plugin disable closes ktinventory viewers`() {
        val player = server.addPlayer()
        val inventory = CloseTrackingInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        server.pluginManager.callEvent(PluginDisableEvent(plugin))

        assertEquals(1, inventory.closeCount)
    }

    @Test
    fun `plugin disable closes paginated and sequence viewers once with shared handler identity`() {
        val paginatedPlayer = server.addPlayer()
        val sequencePlayer = server.addPlayer()
        val paginated = CloseTrackingPaginatedInventory(KtInventoryPluginContext(plugin))
        val sequence = CloseTrackingSequenceInventory(KtInventoryPluginContext(plugin))

        paginated.open(paginatedPlayer)
        sequence.open(sequencePlayer)
        server.pluginManager.callEvent(PluginDisableEvent(plugin))

        assertEquals(1, paginated.closeCount)
        assertEquals(1, sequence.closeCount)
    }

    @Test
    fun `plugin disable for another plugin leaves ktinventory viewers open`() {
        val player = server.addPlayer()
        val otherPlugin = MockBukkit.createMockPlugin()
        val inventory = CloseTrackingInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)
        server.pluginManager.callEvent(PluginDisableEvent(otherPlugin))

        assertEquals(0, inventory.closeCount)
        assertSame(inventory, getTopInventory<CloseTrackingInventory>(player))
    }

    @Test
    fun `same custom context registers handler only once`() {
        val context = CountingContext(plugin)
        val first = TestInventory(context)
        val second = TestInventory(context)

        first.open(server.addPlayer())
        second.open(server.addPlayer())

        assertEquals(1, context.registerCount)
    }

    @Test
    fun `registered handler ignores non ktinventory events`() {
        val player = server.addPlayer()
        val inventory = CloseTrackingInventory(KtInventoryPluginContext(plugin))
        val plainInventory = Bukkit.createInventory(null, 9)

        inventory.open(player)
        player.openInventory(plainInventory)
        assertEquals(1, inventory.closeCount)

        val event = player.clickInventorySlot(0)
        player.closeInventory()

        assertFalse(event.isCancelled)
        assertEquals(1, inventory.closeCount)
    }

    private class TestInventory(
        context: KtInventoryPluginContext,
        line: Int = 1,
    ) : KtInventory(context, line) {
        override fun title() = "Test"

        init {
            button(0, ItemStack(Material.STONE))
        }
    }

    private class EmptyInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        override fun title() = "Empty"
    }

    @Suppress("DEPRECATION")
    private class DeprecatedConstructorInventory(
        plugin: org.bukkit.plugin.Plugin,
    ) : KtInventory(plugin, 1) {
        override fun title() = "Deprecated"
    }

    private class NoColorInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1, null) {
        override fun title() = "&NoColor"
    }

    private class CallbackInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var openCount = 0
            private set
        var buttonClickCount = 0
            private set
        var topClickCount = 0
            private set
        var bottomClickCount = 0
            private set
        var lastButtonPlayer: org.bukkit.entity.HumanEntity? = null
            private set
        var lastButtonSlot: Int? = null
            private set
        var lastButtonClick: ClickType? = null
            private set
        var lastButtonAction: InventoryAction? = null
            private set

        override fun title() = "Callback"

        override fun onOpen(event: org.bukkit.event.inventory.InventoryOpenEvent) {
            openCount += 1
        }

        override fun onClick(event: org.bukkit.event.inventory.InventoryClickEvent) {
            topClickCount += 1
        }

        override fun onClickBottom(event: org.bukkit.event.inventory.InventoryClickEvent) {
            bottomClickCount += 1
        }

        init {
            button(0, ItemStack(Material.STONE)) { event ->
                buttonClickCount += 1
                lastButtonPlayer = event.player
                lastButtonSlot = event.slot
                lastButtonClick = event.click
                lastButtonAction = event.action
                event.currentItem
                event.cursor
                event.hotbarButton
                event.slotType
                @Suppress("DEPRECATION")
                event.unsafe()
            }
        }
    }

    private class JoinedButtonInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        val clicks = mutableListOf<String>()

        override fun title() = "Joined"

        init {
            val button =
                createButton(ItemStack(Material.STONE)) {
                    clicks += "original"
                }.join {
                    clicks += "joined"
                }
            button(0, button)
        }
    }

    private class StorableInventory(
        context: KtInventoryPluginContext,
        private val saved: MutableList<List<ItemStack?>>,
    ) : KtInventory(context, 1) {
        var preClickCount = 0
            private set
        var clickCount = 0
            private set
        var lastClickPlayer: org.bukkit.entity.HumanEntity? = null
            private set
        var lastClickSlot: Int? = null
            private set
        var lastClickType: ClickType? = null
            private set
        var lastClickAction: InventoryAction? = null
            private set

        override fun title() = "Storable"

        init {
            storable(
                0,
                1,
                initialize = { listOf(ItemStack(Material.DIRT), ItemStack(Material.STONE)) },
                onPreClick = { event ->
                    preClickCount += 1
                    lastClickPlayer = event.player
                    lastClickSlot = event.slot
                    lastClickType = event.click
                    lastClickAction = event.action
                    event.currentItem
                    event.cursor
                    event.hotbarButton
                    event.slotType
                    @Suppress("DEPRECATION")
                    event.unsafe()
                    KtInventoryStorable.EventResult.Allow
                },
                onClick = {
                    clickCount += 1
                },
                save = {
                    saved += it
                },
            )
        }
    }

    private class DragInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var preDragCount = 0
            private set
        var dragCount = 0
            private set
        var lastDragPlayer: org.bukkit.entity.HumanEntity? = null
            private set
        var lastDragSlots: Set<Int>? = null
            private set
        var lastDragRawSlots: Set<Int>? = null
            private set
        var lastDragType: DragType? = null
            private set
        var lastDragCursor: ItemStack? = null
            private set
        var lastDragOldCursor: ItemStack? = null
            private set
        var lastDragNewItems: Map<Int, ItemStack>? = null
            private set

        override fun title() = "Drag"

        override fun onDrag(event: org.bukkit.event.inventory.InventoryDragEvent) {
            assertEquals(InventoryType.CHEST, event.inventory.type)
        }

        init {
            storable(
                0,
                onPreDrag = { event ->
                    preDragCount += 1
                    lastDragPlayer = event.player
                    lastDragSlots = event.slots
                    lastDragRawSlots = event.rawSlots
                    lastDragType = event.type
                    lastDragCursor = event.cursor
                    lastDragOldCursor = event.oldCursor
                    lastDragNewItems = event.newItems
                    @Suppress("DEPRECATION")
                    event.unsafe()
                    KtInventoryStorable.EventResult.Allow
                },
                onDrag = {
                    dragCount += 1
                },
            )
        }
    }

    private class DenyClickInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var preClickCount = 0
            private set
        var clickCount = 0
            private set

        override fun title() = "DenyClick"

        init {
            storable(
                0,
                onPreClick = {
                    preClickCount += 1
                    KtInventoryStorable.EventResult.Deny
                },
                onClick = {
                    clickCount += 1
                },
            )
        }
    }

    private class DenyDragInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var preDragCount = 0
            private set
        var dragCount = 0
            private set

        override fun title() = "DenyDrag"

        init {
            storable(
                0,
                onPreDrag = {
                    preDragCount += 1
                    KtInventoryStorable.EventResult.Deny
                },
                onDrag = {
                    dragCount += 1
                },
            )
        }
    }

    private class UnmanagedStorableInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var preClickCount = 0
            private set
        var clickCount = 0
            private set
        var preDragCount = 0
            private set
        var dragCount = 0
            private set

        override fun title() = "Unmanaged"

        init {
            storable(
                1,
                onPreClick = {
                    preClickCount += 1
                    KtInventoryStorable.EventResult.Allow
                },
                onClick = {
                    clickCount += 1
                },
                onPreDrag = {
                    preDragCount += 1
                    KtInventoryStorable.EventResult.Allow
                },
                onDrag = {
                    dragCount += 1
                },
            )
        }
    }

    private class CloseTrackingInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var closeCount = 0
            private set

        override fun title() = "Close"

        override fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
            closeCount += 1
        }
    }

    private class CloseTrackingPaginatedInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginated(context, 1) {
        var closeCount = 0
            private set

        override val entries =
            (0 until 2).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = "Paginated"

        override fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
            closeCount += 1
        }

        init {
            paginateSlot(0)
        }
    }

    private class CloseTrackingSequenceInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryPaginatedSequence(context, 1) {
        var closeCount = 0
            private set

        override val entries
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = "Sequence"

        override fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
            closeCount += 1
        }

        init {
            paginateSlot(0)
        }
    }

    private class CountingContext(
        private val plugin: org.bukkit.plugin.Plugin,
    ) : KtInventoryPluginContext {
        override val handlerId = KtInventoryHandlerId.of(plugin)

        var registerCount = 0
            private set

        override fun registerEvents(listener: Listener) {
            registerCount += 1
            plugin.server.pluginManager.registerEvents(listener, plugin)
        }
    }
}
