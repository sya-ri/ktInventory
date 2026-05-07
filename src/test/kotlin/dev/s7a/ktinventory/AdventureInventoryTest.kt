package dev.s7a.ktinventory

import dev.s7a.ktinventory.components.KtInventoryButton
import dev.s7a.ktinventory.util.getTopInventoryPaginated
import dev.s7a.ktinventory.util.getTopInventorySequenceEntry
import net.kyori.adventure.text.Component
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
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AdventureInventoryTest {
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
    fun `adventure inventory opens with component title`() {
        val player = server.addPlayer()
        val inventory = TestAdventureInventory(KtInventoryPluginContext(plugin))

        inventory.open(player)

        assertSame(inventory, player.openInventory.topInventory.holder)
        assertEquals(Component.text("Adventure"), player.openInventory.title())
    }

    @Test
    @Suppress("DEPRECATION")
    fun `deprecated adventure plugin constructor still opens`() {
        val player = server.addPlayer()
        val inventory = DeprecatedAdventureInventory(plugin)

        inventory.open(player)

        assertSame(inventory, player.openInventory.topInventory.holder)
    }

    @Test
    fun `paginated adventure inventory opens requested page`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedAdventureInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertSame(inventory, entry.paginated)
        assertEquals(1, entry.page)
        assertEquals(Component.text("Adventure 2/2"), player.openInventory.title())
        assertSame(inventory, getTopInventoryPaginated<TestPaginatedAdventureInventory>(player))
        assertNotNull(player.openInventory.topInventory.getItem(0))
    }

    @Test
    @Suppress("DEPRECATION")
    fun `deprecated paginated adventure plugin constructor still opens requested page`() {
        val player = server.addPlayer()
        val inventory = DeprecatedPaginatedAdventureInventory(plugin)

        inventory.open(player, 1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertSame(inventory, entry.paginated)
        assertEquals(1, entry.page)
    }

    @Test
    fun `paginated adventure refreshable replaces inventory`() {
        val player = server.addPlayer()
        val inventory = TestPaginatedAdventureInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)
        TestPaginatedAdventureInventory.refresh(player)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventoryPaginated.Entry<*>
        assertEquals(0, entry.page)
        assertTrue(entry.paginated is TestPaginatedAdventureInventory)
    }

    @Test
    fun `sequence adventure inventory opens requested page`() {
        val player = server.addPlayer()
        val inventory = TestSequenceAdventureInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        assertSame(inventory, entry.paginated)
        assertEquals(1, entry.page)
        assertEquals(Component.text("Adventure 2"), player.openInventory.title())
        assertSame(entry as Any?, getTopInventorySequenceEntry<TestSequenceAdventureInventory>(player))
        assertNotNull(player.openInventory.topInventory.getItem(0))
    }

    @Test
    fun `sequence adventure refreshable replaces inventory`() {
        val player = server.addPlayer()
        val inventory = TestSequenceAdventureInventory(KtInventoryPluginContext(plugin))

        inventory.open(player, 1)
        TestSequenceAdventureInventory.refresh(player)

        val entry = player.openInventory.topInventory.holder as AbstractKtInventorySequence.Entry<*>
        assertEquals(0, entry.page)
        assertTrue(entry.paginated is TestSequenceAdventureInventory)
    }

    private class TestAdventureInventory(
        context: KtInventoryPluginContext,
    ) : KtInventoryAdventure(context, 1) {
        override fun title() = Component.text("Adventure")
    }

    @Suppress("DEPRECATION")
    private class DeprecatedAdventureInventory(
        plugin: org.bukkit.plugin.Plugin,
    ) : KtInventoryAdventure(plugin, 1) {
        override fun title() = Component.text("Deprecated")
    }

    private class TestPaginatedAdventureInventory(
        private val context: KtInventoryPluginContext,
    ) : KtInventoryPaginatedAdventure(context, 1) {
        companion object :
            KtInventoryPaginatedAdventure.Refreshable<TestPaginatedAdventureInventory>(TestPaginatedAdventureInventory::class) {
            override fun createNew(
                player: org.bukkit.entity.HumanEntity,
                inventory: AbstractKtInventoryPaginated.Entry<TestPaginatedAdventureInventory>,
            ) = TestPaginatedAdventureInventory(inventory.paginated.context)
        }

        override val entries =
            (0 until 3).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = Component.text("Adventure ${page + 1}/${lastPage + 1}")

        init {
            paginateSlot(0, 1)
        }
    }

    @Suppress("DEPRECATION")
    private class DeprecatedPaginatedAdventureInventory(
        plugin: org.bukkit.plugin.Plugin,
    ) : KtInventoryPaginatedAdventure(plugin, 1) {
        override val entries =
            (0 until 3).map {
                createButton(ItemStack(Material.STONE)) {}
            }

        override fun title(
            page: Int,
            lastPage: Int,
        ) = Component.text("Deprecated")

        init {
            paginateSlot(0, 1)
        }
    }

    private class TestSequenceAdventureInventory(
        private val context: KtInventoryPluginContext,
    ) : KtInventorySequenceAdventure(context, 1) {
        companion object :
            KtInventorySequenceAdventure.Refreshable<TestSequenceAdventureInventory>(TestSequenceAdventureInventory::class) {
            override fun createNew(
                player: org.bukkit.entity.HumanEntity,
                inventory: AbstractKtInventorySequence.Entry<TestSequenceAdventureInventory>,
            ) = TestSequenceAdventureInventory(inventory.paginated.context)
        }

        override val entries: Sequence<KtInventoryButton<AbstractKtInventorySequence.Entry<KtInventorySequenceAdventure>>>
            get() =
                generateSequence {
                    createButton(ItemStack(Material.STONE)) {}
                }

        override fun title(page: Int) = Component.text("Adventure ${page + 1}")

        init {
            paginateSlot(0, 1)
        }
    }
}
