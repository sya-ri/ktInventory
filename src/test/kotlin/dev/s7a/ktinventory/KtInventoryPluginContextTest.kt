package dev.s7a.ktinventory

import org.bukkit.event.Listener
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KtInventoryPluginContextTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
    }

    @AfterTest
    fun tearDown() {
        KtInventoryHandlerId.remove(plugin)
        MockBukkit.unmock()
    }

    @Test
    fun `plugin contexts share handler id per plugin instance`() {
        val first = KtInventoryPluginContext(plugin)
        val second = KtInventoryPluginContext(plugin)

        assertSame(first.handlerId, second.handlerId)
        assertSame(first.handlerId, KtInventoryHandlerId.find(plugin))
    }

    @Test
    fun `plugin contexts use different handler ids for different plugin instances`() {
        val otherPlugin = MockBukkit.createMockPlugin()

        assertNotSame(
            KtInventoryPluginContext(plugin).handlerId,
            KtInventoryPluginContext(otherPlugin).handlerId,
        )
    }

    @Test
    fun `custom context handler id is stable for the same context instance`() {
        val context = TestContext(plugin)

        assertSame(context.handlerId, context.handlerId)
        assertSame(context.handlerId, KtInventoryHandlerId.find(plugin))
    }

    @Test
    fun `custom contexts can share handler id through the plugin that registers events`() {
        assertSame(TestContext(plugin).handlerId, TestContext(plugin).handlerId)
    }

    @Test
    fun `lazy fetchable context schedules sync and async tasks with the expected scheduler mode`() {
        val context = KtInventoryPluginContext.LazyFetchable(plugin)

        val syncTask = context.runTask {}
        val asyncTask = context.runTaskAsync {}

        assertTrue(syncTask.isSync)
        assertFalse(asyncTask.isSync)
    }

    private class TestContext(
        plugin: PluginMock,
    ) : KtInventoryPluginContext {
        override val handlerId = KtInventoryHandlerId.of(plugin)

        override fun registerEvents(listener: Listener) {}
    }
}
