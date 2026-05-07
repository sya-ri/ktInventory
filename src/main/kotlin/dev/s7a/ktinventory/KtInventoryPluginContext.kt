package dev.s7a.ktinventory

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
 * Context interface for KtInventory plugin integration.
 *
 * This interface provides methods for registering event listeners within the plugin context.
 *
 * @since 2.1.0
 */
interface KtInventoryPluginContext {
    /**
     * Identifier used to share the internal inventory event handler.
     *
     * Contexts created with [KtInventoryPluginContext.invoke] share this identifier per plugin instance.
     * Custom contexts should use [KtInventoryHandlerId.of] with the plugin that registers events.
     *
     * @since 2.2.0
     */
    val handlerId: KtInventoryHandlerId

    /**
     * Registers an event listener with the plugin.
     *
     * @param listener The Bukkit event listener to be registered
     * @since 2.1.0
     */
    fun registerEvents(listener: Listener)

    /**
     * Context capability for inventories that fetch data asynchronously.
     *
     * Custom contexts only need to implement this interface when they are used with lazy fetched inventories.
     */
    interface LazyFetchable : KtInventoryPluginContext {
        /**
         * Runs a task on the server main thread.
         *
         * @param block Task to run
         * @return Scheduled task
         * @since 2.2.0
         */
        fun runTask(block: () -> Unit): BukkitTask

        /**
         * Runs a task asynchronously.
         *
         * @param block Task to run
         * @return Scheduled task
         * @since 2.2.0
         */
        fun runTaskAsync(block: () -> Unit): BukkitTask

        companion object {
            /**
             * Creates a [LazyFetchable] context for the specified [plugin].
             *
             * @since 2.2.0
             */
            operator fun invoke(plugin: Plugin) =
                object : LazyFetchable {
                    override val handlerId = KtInventoryHandlerId.of(plugin)

                    override fun registerEvents(listener: Listener) {
                        plugin.server.pluginManager.registerEvents(listener, plugin)
                    }

                    override fun runTask(block: () -> Unit) = plugin.server.scheduler.runTask(plugin, Runnable(block))

                    override fun runTaskAsync(block: () -> Unit) = plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable(block))
                }
        }
    }

    companion object {
        /**
         * Creates a [KtInventoryPluginContext] instance for the specified [plugin].
         *
         * @since 2.1.0
         */
        operator fun invoke(plugin: Plugin) =
            object : KtInventoryPluginContext {
                override val handlerId = KtInventoryHandlerId.of(plugin)

                override fun registerEvents(listener: Listener) {
                    plugin.server.pluginManager.registerEvents(listener, plugin)
                }
            }
    }
}
