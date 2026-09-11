package dev.s7a.ktinventory.options

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPluginContext
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
import kotlin.test.assertTrue

class KtInventoryStorableOptionTest {
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
    fun `close event saves storable when allowSave returns true`() {
        val state = SaveState()
        val player = server.addPlayer()
        val inventory = TestInventory(KtInventoryPluginContext(plugin), state, allowSave = true)

        inventory.open(player)
        player.closeInventory()

        assertTrue(state.allowSaveCalled)
        assertTrue(state.onSaveCalled)
        assertEquals(listOf(Material.STONE), state.savedItems.single().map { it?.type })
    }

    @Test
    fun `close event does not save storable when allowSave returns false`() {
        val state = SaveState()
        val player = server.addPlayer()
        val inventory = TestInventory(KtInventoryPluginContext(plugin), state, allowSave = false)

        inventory.open(player)
        player.closeInventory()

        assertTrue(state.allowSaveCalled)
        assertFalse(state.onSaveCalled)
        assertEquals(emptyList(), state.savedItems)
    }

    private class SaveState {
        var allowSaveCalled = false
        var onSaveCalled = false
        val savedItems = mutableListOf<List<ItemStack?>>()
    }

    private class TestInventory(
        context: KtInventoryPluginContext,
        private val state: SaveState,
        private val allowSave: Boolean,
    ) : KtInventory(context, 1) {
        override val storableOption =
            buildStorableOption {
                allowSave {
                    state.allowSaveCalled = true
                    allowSave
                }
                onSave {
                    state.onSaveCalled = true
                }
            }

        override fun title() = "Test"

        init {
            storable(
                0,
                initialize = { listOf(ItemStack(Material.STONE)) },
                save = {
                    state.savedItems += it
                },
            )
        }
    }
}
