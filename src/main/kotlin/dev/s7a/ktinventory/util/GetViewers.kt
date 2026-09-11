package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedFetched
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedLazyFetched
import dev.s7a.ktinventory.AbstractKtInventoryPaginatedSequence
import dev.s7a.ktinventory.HasParentInventory
import dev.s7a.ktinventory.KtInventoryBase
import dev.s7a.ktinventory.ParentInventory
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Gets all online players currently viewing an inventory holder of the specified type.
 *
 * @param T The inventory holder type
 * @param clazz The KClass of the inventory holder type
 * @return Map of players to their open inventory holders of type T
 * @since 2.2.0
 */
fun <T : Any> getViewers(clazz: KClass<T>): Map<Player, T> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventory(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing an inventory holder of the specified type.
 *
 * @param T The inventory holder type
 * @return Map of players to their open inventory holders of type T
 * @since 2.2.0
 */
inline fun <reified T : Any> getViewers() = getViewers(T::class)

/**
 * Gets all online players currently viewing a paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open paginated inventory entries of type T
 * @since 2.2.0
 */
fun <T : AbstractKtInventoryPaginated<*>> getViewersPaginatedEntry(clazz: KClass<T>): Map<Player, AbstractKtInventoryPaginated.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventoryPaginatedEntry(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing a paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @return Map of players to their open paginated inventory entries of type T
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginated<*>> getViewersPaginatedEntry() = getViewersPaginatedEntry(T::class)

/**
 * Gets all online players currently viewing a sequence-backed paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginatedSequence]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open sequence-backed inventory entries of type T
 * @since 2.2.0
 */
fun <T : AbstractKtInventoryPaginatedSequence<*>> getViewersPaginatedSequenceEntry(
    clazz: KClass<T>,
): Map<Player, AbstractKtInventoryPaginatedSequence.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventoryPaginatedSequenceEntry(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing a sequence-backed paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginatedSequence]
 * @return Map of players to their open sequence-backed inventory entries of type T
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginatedSequence<*>> getViewersPaginatedSequenceEntry() =
    getViewersPaginatedSequenceEntry(T::class)

/**
 * Gets all online players currently viewing a condition-fetched paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginatedFetched]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open condition-fetched inventory entries for type T
 * @since 2.2.0
 */
fun <T : AbstractKtInventoryPaginatedFetched<*, *>> getViewersPaginatedFetchedEntry(
    clazz: KClass<T>,
): Map<Player, AbstractKtInventoryPaginatedFetched.Entry<*, *>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventoryPaginatedFetchedEntry(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing a condition-fetched paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginatedFetched]
 * @return Map of players to their open condition-fetched inventory entries for type T
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginatedFetched<*, *>> getViewersPaginatedFetchedEntry() =
    getViewersPaginatedFetchedEntry(T::class)

/**
 * Gets all online players currently viewing a lazy-fetched paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginatedLazyFetched]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open lazy-fetched inventory entries for type T
 * @since 2.2.0
 */
fun <T : AbstractKtInventoryPaginatedLazyFetched<*, *, *>> getViewersPaginatedLazyFetchedEntry(
    clazz: KClass<T>,
): Map<Player, AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventoryPaginatedLazyFetchedEntry(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing a lazy-fetched paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginatedLazyFetched]
 * @return Map of players to their open lazy-fetched inventory entries for type T
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventoryPaginatedLazyFetched<*, *, *>> getViewersPaginatedLazyFetchedEntry() =
    getViewersPaginatedLazyFetchedEntry(T::class)

/**
 * Gets all online players currently viewing an inventory or child inventory of the specified parent type.
 * This function searches through the inventory hierarchy to find parent inventories of the specified type.
 *
 * @param T The type of parent inventory
 * @param clazz The KClass of the parent inventory type
 * @return Map of players to their open parent inventories of type T
 * @since 2.2.0
 */
fun <T : ParentInventory> getViewersDeeply(clazz: KClass<T>): Map<Player, T> =
    getViewers<KtInventoryBase>()
        .mapNotNull { (player, inventory) ->
            val parentInventory =
                when (inventory) {
                    is HasParentInventory<*> -> {
                        clazz.safeCast(inventory.parentInventory)
                    }

                    is AbstractKtInventoryPaginated.Entry<*> -> {
                        when (val paginated = inventory.paginated) {
                            is HasParentInventory<*> -> {
                                clazz.safeCast(paginated.parentInventory)
                            }

                            else -> {
                                clazz.safeCast(paginated)
                            }
                        }
                    }

                    is AbstractKtInventoryPaginatedSequence.Entry<*> -> {
                        when (val paginated = inventory.paginated) {
                            is HasParentInventory<*> -> {
                                clazz.safeCast(paginated.parentInventory)
                            }

                            else -> {
                                clazz.safeCast(paginated)
                            }
                        }
                    }

                    is AbstractKtInventoryPaginatedFetched.Entry<*, *> -> {
                        when (val paginated = inventory.paginated) {
                            is HasParentInventory<*> -> {
                                clazz.safeCast(paginated.parentInventory)
                            }

                            else -> {
                                clazz.safeCast(paginated)
                            }
                        }
                    }

                    is AbstractKtInventoryPaginatedLazyFetched.Entry<*, *, *> -> {
                        when (val paginated = inventory.paginated) {
                            is HasParentInventory<*> -> {
                                clazz.safeCast(paginated.parentInventory)
                            }

                            else -> {
                                clazz.safeCast(paginated)
                            }
                        }
                    }

                    else -> {
                        clazz.safeCast(inventory)
                    }
                } ?: return@mapNotNull null
            player to parentInventory
        }.toMap()

/**
 * Gets all online players currently viewing an inventory or child inventory of the specified parent type.
 * This function searches through the inventory hierarchy to find parent inventories of the specified type.
 *
 * @param T The type of parent inventory
 * @return Map of players to their open parent inventories of type T
 * @since 2.2.0
 */
inline fun <reified T : ParentInventory> getViewersDeeply() = getViewersDeeply(T::class)
