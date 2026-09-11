package dev.s7a.ktinventory

import dev.s7a.ktinventory.util.getTopInventory
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class KtInventoryHandlerTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock
    private lateinit var otherPlugin: PluginMock

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin("InventoryOwner")
        otherPlugin = MockBukkit.createMockPlugin("OtherInventoryOwner")
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `events and saves run once when two plugins use inventories`() {
        val inventories =
            listOf(plugin, otherPlugin).map { owner ->
                val player = server.addPlayer()
                val inventory = TrackingInventory(KtInventoryPluginContext(owner))
                inventory.open(player)
                player to inventory
            }

        inventories.forEach { (player, inventory) ->
            player.clickInventorySlot(0)
            player.clickInventorySlot(1)
            player.clickInventorySlot(inventory.size)
            server.pluginManager.callEvent(
                InventoryDragEvent(
                    player.openInventory,
                    ItemStack(Material.STONE),
                    ItemStack(Material.DIRT),
                    false,
                    mapOf(1 to ItemStack(Material.STONE)),
                ),
            )
            player.closeInventory()

            assertEquals(1, inventory.openCount)
            assertEquals(1, inventory.buttonClickCount)
            assertEquals(2, inventory.topClickCount)
            assertEquals(1, inventory.bottomClickCount)
            assertEquals(1, inventory.storableClickCount)
            assertEquals(1, inventory.storableDragCount)
            assertEquals(1, inventory.dragCount)
            assertEquals(1, inventory.closeCount)
            assertEquals(listOf(listOf<Material?>(Material.DIRT)), inventory.saved)
        }
    }

    @Test
    fun `disabling one registered plugin closes and saves only its inventories`() {
        val player = server.addPlayer()
        val otherPlayer = server.addPlayer()
        val inventory = TrackingInventory(KtInventoryPluginContext(plugin))
        val otherInventory = TrackingInventory(KtInventoryPluginContext(otherPlugin))
        inventory.open(player)
        otherInventory.open(otherPlayer)

        server.pluginManager.callEvent(PluginDisableEvent(plugin))

        assertNotSame(inventory, getTopInventory<TrackingInventory>(player))
        assertEquals(1, inventory.closeCount)
        assertEquals(listOf(listOf<Material?>(Material.DIRT)), inventory.saved)
        assertSame(otherInventory, getTopInventory<TrackingInventory>(otherPlayer))
        assertEquals(0, otherInventory.closeCount)
        assertEquals(emptyList(), otherInventory.saved)

        otherPlayer.clickInventorySlot(0)
        otherPlayer.closeInventory()

        assertEquals(1, otherInventory.buttonClickCount)
        assertEquals(1, otherInventory.closeCount)
        assertEquals(listOf(listOf<Material?>(Material.DIRT)), otherInventory.saved)
    }

    private class TrackingInventory(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        var openCount = 0
        var buttonClickCount = 0
        var topClickCount = 0
        var bottomClickCount = 0
        var storableClickCount = 0
        var storableDragCount = 0
        var dragCount = 0
        var closeCount = 0
        val saved = mutableListOf<List<Material?>>()

        override fun title() = "Tracking"

        override fun onOpen(event: InventoryOpenEvent) {
            openCount += 1
        }

        override fun onClick(event: InventoryClickEvent) {
            topClickCount += 1
        }

        override fun onClickBottom(event: InventoryClickEvent) {
            bottomClickCount += 1
        }

        override fun onDrag(event: InventoryDragEvent) {
            dragCount += 1
        }

        override fun onClose(event: InventoryCloseEvent) {
            closeCount += 1
        }

        init {
            button(0, ItemStack(Material.STONE)) { buttonClickCount += 1 }
            storable(
                1,
                initialize = { listOf(ItemStack(Material.DIRT)) },
                onClick = { storableClickCount += 1 },
                onDrag = { storableDragCount += 1 },
                save = { items -> saved += items.map { it?.type } },
            )
        }
    }
}
