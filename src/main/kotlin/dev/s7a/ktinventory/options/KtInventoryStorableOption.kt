package dev.s7a.ktinventory.options

import org.bukkit.event.inventory.InventoryCloseEvent

inline fun buildStorableOption(block: KtInventoryStorableOption.Builder.() -> Unit) =
    KtInventoryStorableOption.Builder().apply(block).build()

interface KtInventoryStorableOption {
    fun allowSave(event: InventoryCloseEvent): Boolean

    val viewOnly: Boolean

    companion object {
        val Default = buildStorableOption {}
    }

    class Builder {
        private var allowSave: (event: InventoryCloseEvent) -> Boolean = { event ->
            event.viewers.size == 1
        }

        private var viewOnly: Boolean = false

        fun allowSave(block: (event: InventoryCloseEvent) -> Boolean) {
            this.allowSave = block
        }

        fun viewOnly() {
            viewOnly = true
        }

        fun build() =
            object : KtInventoryStorableOption {
                override fun allowSave(event: InventoryCloseEvent) = this@Builder.allowSave(event)

                override val viewOnly = this@Builder.viewOnly
            }
    }
}
