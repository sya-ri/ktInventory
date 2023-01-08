import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.MockPlugin
import be.seeseemelk.mockbukkit.ServerMock
import dev.s7a.ktinventory.ktInventory
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtInventoryTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MockPlugin

    @BeforeTest
    fun setup() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
    }

    @AfterTest
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun open() {
        val inventory = plugin.ktInventory("test")
        val player = server.addPlayer()
        assertTrue(inventory.bukkitInventory.viewers.isEmpty())
        inventory.open(player)
        player.assertInventoryView(InventoryType.CHEST) {
            it == inventory.bukkitInventory
        }
        player.closeInventory()
        assertTrue(inventory.bukkitInventory.viewers.isEmpty())
    }

    @Test
    fun place_item() {
        val inventory = plugin.ktInventory("test") {
            item(2, ItemStack(Material.STONE))
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.assertInventoryView(InventoryType.CHEST) {
            it.getItem(2) == ItemStack(Material.STONE)
        }
    }

    @Test
    fun update_item() {
        val inventory = plugin.ktInventory("test") {
            item(2, ItemStack(Material.STONE))
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.assertInventoryView(InventoryType.CHEST) {
            it.getItem(2) == ItemStack(Material.STONE)
        }
        inventory.item(2, ItemStack(Material.DIRT))
        player.assertInventoryView(InventoryType.CHEST) {
            it.getItem(2) == ItemStack(Material.DIRT)
        }
    }

    @Test
    fun click_item() {
        val clickCount = AtomicInteger()
        val inventory = plugin.ktInventory("test") {
            item(2, ItemStack(Material.STONE)) {
                clickCount.incrementAndGet()
            }
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.simulateInventoryClick(2)
        player.simulateInventoryClick(3)
        assertEquals(1, clickCount.get())
    }

    @Test
    fun update_item_click_action() {
        val clickCount = AtomicInteger()
        val inventory = plugin.ktInventory("test") {
            item(2, ItemStack(Material.STONE)) {
                clickCount.incrementAndGet()
            }
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.simulateInventoryClick(2)
        assertEquals(1, clickCount.get())
        inventory.item(2, ItemStack(Material.DIRT))
        player.simulateInventoryClick(2)
        assertEquals(2, clickCount.get())
        inventory.item(2, ItemStack(Material.DIRT), null)
        assertEquals(2, clickCount.get())
        inventory.onClick(2) {
            clickCount.incrementAndGet()
        }
        player.simulateInventoryClick(2)
        assertEquals(3, clickCount.get())
    }

    @Test
    fun open_event() {
        val openCount = AtomicInteger()
        val inventory = plugin.ktInventory("test") {
            onOpen {
                openCount.incrementAndGet()
            }
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.closeInventory()
        inventory.open(player)
        assertEquals(2, openCount.get())
    }

    @Test
    fun click_event() {
        val clickCount = AtomicInteger()
        val inventory = plugin.ktInventory("test") {
            onClick {
                clickCount.incrementAndGet()
            }
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.simulateInventoryClick(2)
        player.simulateInventoryClick(3)
        assertEquals(2, clickCount.get())
    }

    @Test
    fun close_event() {
        val closeCount = AtomicInteger()
        val inventory = plugin.ktInventory("test") {
            onClose {
                closeCount.incrementAndGet()
            }
        }
        val player = server.addPlayer()
        inventory.open(player)
        player.closeInventory()
        inventory.open(player)
        assertEquals(1, closeCount.get())
    }
}
