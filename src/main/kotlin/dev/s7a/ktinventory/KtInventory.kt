package dev.s7a.ktinventory

import dev.s7a.ktinventory.internal.KtInventoryHandler
import dev.s7a.ktinventory.internal.KtInventoryImpl
import dev.s7a.ktinventory.internal.color
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

/**
 * Create [KtInventory] from [Inventory].
 *
 * @receiver [Plugin] that handles events.
 * @param bukkitInventory [Inventory] used to generate [KtInventory].
 * @param block Processing using [KtInventory].
 * @since 1.0.0
 */
fun Plugin.ktInventory(bukkitInventory: Inventory, block: KtInventory.() -> Unit): KtInventory {
    val handler = KtInventoryHandler.get(this)
    return KtInventoryImpl(handler, bukkitInventory).apply(block)
}

/**
 * Create [KtInventory] of chest type [Inventory].
 *
 * @receiver [Plugin] that handles events.
 * @param title Inventory title.
 * @param line Inventory size (must be 1-6).
 * @param altColorChar Alternate color code character to use for [title]. If null, do nothing. (default: '&')
 * @param block Processing using [KtInventory].
 * @since 1.0.0
 */
fun Plugin.ktInventory(title: String, line: Int = 3, altColorChar: Char? = '&', block: KtInventory.() -> Unit): KtInventory {
    require(line in 1..6)
    return ktInventory(Bukkit.createInventory(null, line * 9, title.color(altColorChar)), block)
}

/**
 * KtInventory
 *
 * @see ktInventory
 * @since 1.0.0
 */
interface KtInventory {
    /**
     * [Inventory]
     *
     * @since 1.0.0
     */
    val bukkitInventory: Inventory

    /**
     * Set the item. The action on click is not changed, and the previous one is inherited.
     *
     * @param index Slot number to set.
     * @param itemStack [ItemStack] to set.
     * @throws IndexOutOfBoundsException When placing an item outside the inventory contents.
     * @since 1.0.0
     */
    fun item(index: Int, itemStack: ItemStack)

    /**
     * Set the item and the action on click.
     *
     * @param index Slot number to set.
     * @param itemStack [ItemStack] to set.
     * @param block Click action. If null, the action will be removed.
     * @throws IndexOutOfBoundsException When placing an item outside the inventory contents.
     * @since 1.0.0
     */
    fun item(index: Int, itemStack: ItemStack, block: ((InventoryClickEvent) -> Unit)?)

    /**
     * Change the action that is executed before each action of [item] on click.
     * The default is event cancellation.
     *
     * @param block Click action
     * @since 1.0.0
     */
    fun onClick(block: ((InventoryClickEvent) -> Unit)?)

    /**
     * Change the action on closed.
     *
     * @param block Close action
     * @since 1.0.0
     */
    fun onClose(block: ((InventoryCloseEvent) -> Unit)?)

    /**
     * Open the inventory.
     *
     * @param player Player opening inventory.
     * @since 1.0.0
     */
    fun open(player: HumanEntity)
}
