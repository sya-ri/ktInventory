import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.MockPlugin
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.WorldMock
import dev.s7a.ktinventory.KtInventoryProvider
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.Dispenser
import org.bukkit.block.Dropper
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InWorldProviderTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MockPlugin
    private lateinit var world: WorldMock

    @BeforeTest
    fun setup() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin(UUID.randomUUID().toString())
        world = server.addSimpleWorld("test_world")
    }

    @AfterTest
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun inventory_from_chest() {
        block_test<Chest>(Material.CHEST, InventoryType.CHEST)
    }

    @Test
    fun inventory_from_dispenser() {
        block_test<Dispenser>(Material.DISPENSER, InventoryType.DISPENSER)
    }

    @Test
    fun inventory_from_dropper() {
        block_test<Dropper>(Material.DROPPER, InventoryType.DROPPER)
    }

    private inline fun <reified T : Container> block_test(
        material: Material,
        inventoryType: InventoryType,
    ) {
        val block = world.getBlockAt(0, 0, 0)
        block.type = material
        val container = assertIs<T>(block.state)
        val player1 = server.addPlayer()
        val player2 = server.addPlayer()
        val clickCount = AtomicInteger(0)

        // Open without ktInventory
        player1.openInventory(container.inventory)
        player1.assertInventoryView(inventoryType) {
            it.getItem(0) == null
        }

        // Place the item by ktInventory
        val ktInventory =
            KtInventoryProvider(plugin).create(container.inventory) {
                item(0, ItemStack(Material.STONE)) {
                    clickCount.incrementAndGet()
                }
            }
        player1.assertInventoryView(inventoryType) {
            it.getItem(0) == ItemStack(Material.STONE)
        }

        // Click event is ignored unless via ktInventory
        player1.simulateInventoryClick(0)
        assertEquals(0, clickCount.get())

        // Open with ktInventory
        ktInventory.open(player2)
        player2.assertInventoryView(inventoryType) {
            it.getItem(0) == ItemStack(Material.STONE)
        }
        assertEquals(0, clickCount.get())

        // Call click event
        player2.simulateInventoryClick(0)
        assertEquals(1, clickCount.get())

        // Click event is ignored unless via ktInventory
        player1.simulateInventoryClick(0)
        assertEquals(1, clickCount.get())

        // Items are carried over even if ktInventory is closed
        player2.closeInventory()
        player1.assertInventoryView(inventoryType) {
            it.getItem(0) == ItemStack(Material.STONE)
        }

        // Click event is ignored unless via ktInventory
        player1.simulateInventoryClick(0)
        assertEquals(1, clickCount.get())
    }
}
