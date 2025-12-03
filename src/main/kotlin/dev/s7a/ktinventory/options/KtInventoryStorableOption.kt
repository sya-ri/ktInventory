package dev.s7a.ktinventory.options

import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Creates a new [KtInventoryStorableOption] using the builder pattern.
 *
 * @param block The configuration block for building the option
 * @return A new [KtInventoryStorableOption] instance
 * @since 2.0.0
 */
inline fun buildStorableOption(block: KtInventoryStorableOption.Builder.() -> Unit) =
    KtInventoryStorableOption.Builder().apply(block).build()

/**
 * Interface defining options for storable inventories.
 *
 * @since 2.0.0
 */
interface KtInventoryStorableOption {
    /**
     * Determines if the inventory should be saved when closed.
     *
     * @param event The inventory close event
     * @return true if the inventory should be saved, false otherwise
     * @since 2.0.0
     */
    fun allowSave(event: InventoryCloseEvent): Boolean

    /**
     * Called when the inventory is saved.
     *
     * @since 2.0.0
     */
    fun onSave()

    companion object {
        /**
         * Default storable option implementation.
         *
         * @since 2.0.0
         */
        val Default = buildStorableOption {}
    }

    /**
     * Builder class for creating [KtInventoryStorableOption] instances.
     *
     * @since 2.0.0
     */
    class Builder {
        private var allowSave: (event: InventoryCloseEvent) -> Boolean = { event ->
            event.viewers.size == 1
        }

        private var onSave: () -> Unit = {}

        /**
         * Sets the condition for allowing inventory save.
         *
         * @param block The condition block that determines if save is allowed
         * @since 2.0.0
         */
        fun allowSave(block: (event: InventoryCloseEvent) -> Boolean) {
            this.allowSave = block
        }

        /**
         * Sets the action to be performed on save.
         *
         * @param block The action block to execute when saving
         * @since 2.0.0
         */
        fun onSave(block: () -> Unit) {
            this.onSave = block
        }

        /**
         * Builds and returns a new [KtInventoryStorableOption] instance.
         *
         * @return A new [KtInventoryStorableOption] instance
         * @since 2.0.0
         */
        fun build() =
            object : KtInventoryStorableOption {
                override fun allowSave(event: InventoryCloseEvent) = this@Builder.allowSave(event)

                override fun onSave() = this@Builder.onSave()
            }
    }
}
