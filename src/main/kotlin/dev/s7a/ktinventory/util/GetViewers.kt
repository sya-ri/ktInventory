package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventoryFetched
import dev.s7a.ktinventory.AbstractKtInventoryLazyFetched
import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import dev.s7a.ktinventory.AbstractKtInventorySequence
import dev.s7a.ktinventory.HasParentInventory
import dev.s7a.ktinventory.KtInventoryBase
import dev.s7a.ktinventory.ParentInventory
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.reflect.KClass

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
 * Gets all online players currently viewing a paginated inventory of the specified type.
 *
 * @param T The paginated inventory type
 * @param clazz The KClass of the paginated inventory type
 * @return Map of players to their open paginated inventories of type T
 * @since 2.2.0
 */
fun <T : Any> getViewersPaginated(clazz: KClass<T>): Map<Player, T> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventoryPaginated(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing a paginated inventory of the specified type.
 *
 * @param T The paginated inventory type
 * @return Map of players to their open paginated inventories of type T
 * @since 2.2.0
 */
inline fun <reified T : Any> getViewersPaginated() = getViewersPaginated(T::class)

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
 * @param T The type of inventory extending [AbstractKtInventorySequence]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open sequence-backed inventory entries of type T
 * @since 2.2.0
 */
fun <T : AbstractKtInventorySequence<*>> getViewersSequenceEntry(clazz: KClass<T>): Map<Player, AbstractKtInventorySequence.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventorySequenceEntry(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing a sequence-backed paginated inventory entry of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventorySequence]
 * @return Map of players to their open sequence-backed inventory entries of type T
 * @since 2.2.0
 */
inline fun <reified T : AbstractKtInventorySequence<*>> getViewersSequenceEntry() = getViewersSequenceEntry(T::class)

/**
 * Gets all online players currently viewing an inventory or child inventory of the specified parent type.
 * This function searches through the inventory hierarchy to find parent inventories of the specified type.
 *
 * @param T The type of parent inventory
 * @return Map of players to their open parent inventories of type T
 * @since 2.2.0
 */
inline fun <reified T : ParentInventory> getViewersDeeply() =
    getViewers<KtInventoryBase>()
        .mapNotNull { (player, inventory) ->
            val parentInventory =
                when (inventory) {
                    is T -> {
                        inventory
                    }

                    is HasParentInventory<*> -> {
                        inventory.parentInventory as? T
                    }

                    is AbstractKtInventoryPaginated.Entry<*> -> {
                        when (val paginated = inventory.paginated) {
                            is T -> {
                                paginated
                            }

                            is HasParentInventory<*> -> {
                                paginated.parentInventory as? T
                            }

                            else -> {
                                null
                            }
                        }
                    }

                    is AbstractKtInventorySequence.Entry<*> -> {
                        when (val paginated = inventory.paginated) {
                            is T -> {
                                paginated
                            }

                            is HasParentInventory<*> -> {
                                paginated.parentInventory as? T
                            }

                            else -> {
                                null
                            }
                        }
                    }

                    is AbstractKtInventoryFetched.Entry<*, *> -> {
                        when (val paginated = inventory.paginated) {
                            is T -> {
                                paginated
                            }

                            is HasParentInventory<*> -> {
                                paginated.parentInventory as? T
                            }

                            else -> {
                                null
                            }
                        }
                    }

                    is AbstractKtInventoryLazyFetched.Entry<*, *, *> -> {
                        when (val paginated = inventory.paginated) {
                            is T -> {
                                paginated
                            }

                            is HasParentInventory<*> -> {
                                paginated.parentInventory as? T
                            }

                            else -> {
                                null
                            }
                        }
                    }

                    else -> {
                        null
                    }
                } ?: return@mapNotNull null
            player to parentInventory
        }.toMap()
