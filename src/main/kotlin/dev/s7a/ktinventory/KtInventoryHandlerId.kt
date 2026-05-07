package dev.s7a.ktinventory

import org.bukkit.plugin.Plugin
import java.util.IdentityHashMap

/**
 * Identifier used to share the internal inventory event handler.
 *
 * @since 2.2.0
 */
class KtInventoryHandlerId private constructor() {
    companion object {
        private val ids = IdentityHashMap<Any, KtInventoryHandlerId>()

        /**
         * Gets the handler identifier associated with the specified plugin.
         *
         * @param plugin Plugin that owns the registered inventory event handler
         * @return Handler identifier shared by contexts for the same plugin instance
         * @since 2.2.0
         */
        @Synchronized
        fun of(plugin: Plugin): KtInventoryHandlerId = ids.getOrPut(plugin) { KtInventoryHandlerId() }

        @Synchronized
        internal fun find(plugin: Plugin): KtInventoryHandlerId? = ids[plugin]

        @Synchronized
        internal fun remove(plugin: Plugin) {
            ids.remove(plugin)
        }
    }
}
