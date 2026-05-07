package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedFetched
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedLazyFetched
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedSequence
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Get the upper inventory involved in this transaction via reflection.
 *
 * https://www.spigotmc.org/threads/inventoryview-changed-to-interface-backwards-compatibility.651754/#post-4747875
 *
 * @param viewer The human entity whose top inventory to get
 * @return The top inventory, or null if the viewer has no top inventory
 * @throws RuntimeException if the reflection operation fails
 * @since 2.0.0
 */
private fun getTopBukkitInventory(viewer: HumanEntity): Inventory? =
    try {
        val view = viewer.openInventory
        val getTopInventory = view.javaClass.getMethod("getTopInventory")
        getTopInventory.setAccessible(true)
        getTopInventory.invoke(view) as? Inventory
    } catch (e: Throwable) {
        throw RuntimeException(e)
    }

/**
 * Gets the holder of the top inventory in the currently open inventory view.
 *
 * @param T The inventory holder type
 * @param clazz The KClass of the inventory holder type
 * @param player The player whose top inventory holder to check
 * @return The top inventory holder of type T, or null if not found
 * @since 2.2.0
 */
fun <T : Any> getTopInventory(
    clazz: KClass<T>,
    player: HumanEntity,
): T? = clazz.safeCast(getTopBukkitInventory(player)?.holder)

/**
 * Gets the holder of the top inventory in the currently open inventory view.
 *
 * @param T The inventory holder type
 * @param player The player whose top inventory holder to check
 * @return The top inventory holder of type T, or null if not found
 * @since 2.2.0
 */
inline fun <reified T : Any> getTopInventory(player: HumanEntity) = getTopInventory(T::class, player)

/**
 * Gets the paginated inventory associated with the top inventory in the currently open inventory view.
 *
 * @param T The paginated inventory type
 * @param clazz The KClass of the paginated inventory type
 * @param player The player whose top inventory to check
 * @return The paginated inventory of type T, or null if not found
 * @since 2.2.0
 */
fun <T : Any> getTopInventoryPaginated(
    clazz: KClass<T>,
    player: HumanEntity,
): T? {
    val holder = getTopInventory<Any>(player) ?: return null
    val paginated =
        when (holder) {
            is AbstractKtInventoryPaginated.Entry<*> -> holder.paginated
            is AbstractKtInventoryPaginatedSequence.Entry<*> -> holder.paginated
            is AbstractKtInventoryPaginatedFetched.Entry<*, *> -> holder.paginated
            is AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *> -> holder.paginated
            else -> return null
        }
    return clazz.safeCast(paginated)
}

/**
 * Gets the paginated inventory associated with the top inventory in the currently open inventory view.
 *
 * @param T The paginated inventory type
 * @param player The player whose top inventory to check
 * @return The paginated inventory of type T, or null if not found
 * @since 2.2.0
 */
inline fun <reified T : Any> getTopInventoryPaginated(player: HumanEntity) = getTopInventoryPaginated(T::class, player)

/**
 * Gets the paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The paginated inventory type
 * @param clazz The KClass of the paginated inventory type
 * @param player The player whose top inventory entry to check
 * @return The top paginated inventory entry of type T, or null if not found
 * @since 2.2.0
 */
@Suppress("UNCHECKED_CAST")
fun <T : AbstractKtInventoryPaginated<*>> getTopInventoryPaginatedEntry(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginated.Entry<T>? {
    val entry = getTopInventory<AbstractKtInventoryPaginated.Entry<*>>(player) ?: return null
    if (!clazz.isInstance(entry.paginated)) return null
    return entry as AbstractKtInventoryPaginated.Entry<T>
}

/**
 * Gets the paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The paginated inventory type
 * @param player The player whose top inventory entry to check
 * @return The top paginated inventory entry of type T, or null if not found
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginated<*>> getTopInventoryPaginatedEntry(player: HumanEntity) =
    getTopInventoryPaginatedEntry(T::class, player)

/**
 * Gets the sequence-backed paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The sequence-backed inventory type
 * @param clazz The KClass of the sequence-backed inventory type
 * @param player The player whose top inventory entry to check
 * @return The top sequence-backed inventory entry of type T, or null if not found
 * @since 2.2.0
 */
@Suppress("UNCHECKED_CAST")
fun <T : AbstractKtInventoryPaginatedSequence<*>> getTopInventoryPaginatedSequenceEntry(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginatedSequence.Entry<T>? {
    val entry = getTopInventory<AbstractKtInventoryPaginatedSequence.Entry<*>>(player) ?: return null
    if (!clazz.isInstance(entry.paginated)) return null
    return entry as AbstractKtInventoryPaginatedSequence.Entry<T>
}

/**
 * Gets the sequence-backed paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The sequence-backed inventory type
 * @param player The player whose top inventory entry to check
 * @return The top sequence-backed inventory entry of type T, or null if not found
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginatedSequence<*>> getTopInventoryPaginatedSequenceEntry(player: HumanEntity) =
    getTopInventoryPaginatedSequenceEntry(T::class, player)

/**
 * Gets the condition-fetched paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The condition-fetched inventory type
 * @param clazz The KClass of the condition-fetched inventory type
 * @param player The player whose top inventory entry to check
 * @return The top condition-fetched inventory entry for type T, or null if not found
 * @since 2.2.0
 */
fun <T : AbstractKtInventoryPaginatedFetched<*, *>> getTopInventoryPaginatedFetchedEntry(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginatedFetched.Entry<*, *>? {
    val entry = getTopInventory<AbstractKtInventoryPaginatedFetched.Entry<*, *>>(player) ?: return null
    if (!clazz.isInstance(entry.paginated)) return null
    return entry
}

/**
 * Gets the condition-fetched paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The condition-fetched inventory type
 * @param player The player whose top inventory entry to check
 * @return The top condition-fetched inventory entry for type T, or null if not found
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginatedFetched<*, *>> getTopInventoryPaginatedFetchedEntry(player: HumanEntity) =
    getTopInventoryPaginatedFetchedEntry(T::class, player)

/**
 * Gets the lazy-fetched paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The lazy-fetched inventory type
 * @param clazz The KClass of the lazy-fetched inventory type
 * @param player The player whose top inventory entry to check
 * @return The top lazy-fetched inventory entry for type T, or null if not found
 * @since 2.2.0
 */
fun <T : AbstractKtInventoryPaginatedLazyFetched<*, *, *>> getTopInventoryPaginatedLazyFetchedEntry(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>? {
    val entry = getTopInventory<AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>>(player) ?: return null
    if (!clazz.isInstance(entry.paginated)) return null
    return entry
}

/**
 * Gets the lazy-fetched paginated inventory entry of the top inventory in the currently open inventory view.
 *
 * @param T The lazy-fetched inventory type
 * @param player The player whose top inventory entry to check
 * @return The top lazy-fetched inventory entry for type T, or null if not found
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginatedLazyFetched<*, *, *>> getTopInventoryPaginatedLazyFetchedEntry(player: HumanEntity) =
    getTopInventoryPaginatedLazyFetchedEntry(T::class, player)
