package dev.s7a.ktinventory.components

import dev.s7a.ktinventory.AbstractKtInventory
import org.bukkit.inventory.ItemStack

/**
 * Represents a storable component owned by a paged inventory.
 *
 * This component is applied to each created page entry so initialization and saving can use page-specific state.
 *
 * @param T Type of page entry this storable is applied to
 * @property slots List of slot indices managed by this component
 * @property initialize Callback that provides initial items for a page entry
 * @property onPreClick Callback invoked before click events to determine if action should proceed
 * @property onClick Callback invoked when slots are clicked
 * @property onPreDrag Callback invoked before drag events to determine if action should proceed
 * @property onDrag Callback invoked when items are dragged
 * @since 2.2.0
 */
class KtInventoryPagedStorable<T : AbstractKtInventory> internal constructor(
    val slots: List<Int>,
    val initialize: T.() -> List<ItemStack?>,
    val onPreClick: T.(KtInventoryStorable.ClickEvent) -> KtInventoryStorable.EventResult,
    val onClick: T.(KtInventoryStorable.ClickEvent) -> Unit,
    val onPreDrag: T.(KtInventoryStorable.DragEvent) -> KtInventoryStorable.EventResult,
    val onDrag: T.(KtInventoryStorable.DragEvent) -> Unit,
    private val save: T.(List<ItemStack?>) -> Unit,
) : KtInventoryComponent() {
    internal fun applyTo(inventory: T) {
        inventory.storable(
            slots,
            initialize = { inventory.initialize() },
            onPreClick = { inventory.onPreClick(it) },
            onClick = { inventory.onClick(it) },
            onPreDrag = { inventory.onPreDrag(it) },
            onDrag = { inventory.onDrag(it) },
            save = { inventory.save(it) },
        )
    }
}
