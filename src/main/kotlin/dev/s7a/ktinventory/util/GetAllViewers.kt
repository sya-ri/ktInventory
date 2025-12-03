package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventory
import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import dev.s7a.ktinventory.HasParentInventory
import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.ParentInventory
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.reflect.KClass

/**
 * Gets all online players currently viewing an inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open inventories of type T
 * @since 2.0.0
 */
fun <T : AbstractKtInventory> getAllViewers(clazz: KClass<T>): Map<Player, T> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getOpenInventory(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

/**
 * Gets all online players currently viewing an inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @return Map of players to their open inventories of type T
 * @since 2.0.0
 */
inline fun <reified T : AbstractKtInventory> getAllViewers() = getAllViewers(T::class)

/**
 * Gets all online players currently viewing a paginated inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open paginated inventory entries of type T
 * @since 2.0.0
 */
fun <T : AbstractKtInventoryPaginated<*>> getAllViewersPaginated(clazz: KClass<T>): Map<Player, AbstractKtInventoryPaginated.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getOpenInventoryPaginated(clazz, player) ?: return@mapNotNull null
            player to (inventory)
        }.toMap()

/**
 * Gets all online players currently viewing a paginated inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @return Map of players to their open paginated inventory entries of type T
 * @since 2.0.0
 */
inline fun <reified T : AbstractKtInventoryPaginated<*>> getAllViewersPaginated() = getAllViewersPaginated(T::class)

/**
 * Gets all online players currently viewing an inventory or child inventory of the specified parent type.
 * This function searches through the inventory hierarchy to find parent inventories of the specified type.
 *
 * @param T The type of parent inventory
 * @return Map of players to their open parent inventories of type T
 * @since 2.0.0
 */
inline fun <reified T : ParentInventory> getAllViewersDeeply() =
    getAllViewers<KtInventory>()
        .mapNotNull { (player, inventory) ->
            when (inventory) {
                is T -> inventory
                is HasParentInventory<*> -> inventory.parentInventory as? T
                else -> null
            }?.let {
                player to it
            }
        }.toMap()
