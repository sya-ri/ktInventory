package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventory
import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import dev.s7a.ktinventory.HasParentInventory
import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.ParentInventory
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
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getViewers instead.",
    ReplaceWith("getViewers(clazz)", "dev.s7a.ktinventory.util.getViewers"),
)
fun <T : AbstractKtInventory> getAllViewers(clazz: KClass<T>): Map<Player, T> = getViewers(clazz)

/**
 * Gets all online players currently viewing an inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @return Map of players to their open inventories of type T
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getViewers instead.",
    ReplaceWith("getViewers<T>()", "dev.s7a.ktinventory.util.getViewers"),
)
inline fun <reified T : AbstractKtInventory> getAllViewers() = getViewers<T>()

/**
 * Gets all online players currently viewing a paginated inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param clazz The KClass of the inventory type
 * @return Map of players to their open paginated inventory entries of type T
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getViewersPaginatedEntry instead.",
    ReplaceWith("getViewersPaginatedEntry(clazz)", "dev.s7a.ktinventory.util.getViewersPaginatedEntry"),
)
fun <T : AbstractKtInventoryPaginated<*>> getAllViewersPaginated(clazz: KClass<T>): Map<Player, AbstractKtInventoryPaginated.Entry<T>> =
    getViewersPaginatedEntry(clazz)

/**
 * Gets all online players currently viewing a paginated inventory of the specified type.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @return Map of players to their open paginated inventory entries of type T
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getViewersPaginatedEntry instead.",
    ReplaceWith("getViewersPaginatedEntry<T>()", "dev.s7a.ktinventory.util.getViewersPaginatedEntry"),
)
inline fun <reified T : AbstractKtInventoryPaginated<*>> getAllViewersPaginated() = getViewersPaginatedEntry<T>()

/**
 * Gets all online players currently viewing an inventory or child inventory of the specified parent type.
 * This function searches through the inventory hierarchy to find parent inventories of the specified type.
 *
 * @param T The type of parent inventory
 * @return Map of players to their open parent inventories of type T
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getViewersDeeply instead.",
    ReplaceWith("getViewersDeeply<T>()", "dev.s7a.ktinventory.util.getViewersDeeply"),
    level = DeprecationLevel.WARNING,
)
inline fun <reified T : ParentInventory> getAllViewersDeeply() =
    getViewers<KtInventory>()
        .mapNotNull { (player, inventory) ->
            val parentInventory =
                when (inventory) {
                    is T -> inventory
                    is HasParentInventory<*> -> inventory.parentInventory as? T
                    else -> null
                } ?: return@mapNotNull null
            player to parentInventory
        }.toMap()
