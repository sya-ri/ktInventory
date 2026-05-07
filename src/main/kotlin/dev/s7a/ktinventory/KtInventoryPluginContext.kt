package dev.s7a.ktinventory

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

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
