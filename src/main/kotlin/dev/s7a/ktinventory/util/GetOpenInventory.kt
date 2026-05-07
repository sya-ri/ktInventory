package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventory
import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import org.bukkit.entity.HumanEntity
import kotlin.reflect.KClass

/**
 * Gets the currently open inventory of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @param clazz The KClass of the inventory type
 * @param player The player whose inventory to check
 * @return The open inventory of type T, or null if not found
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getTopInventory instead.",
    ReplaceWith("getTopInventory(clazz, player)", "dev.s7a.ktinventory.util.getTopInventory"),
)
fun <T : AbstractKtInventory> getOpenInventory(
    clazz: KClass<T>,
    player: HumanEntity,
): T? = getTopInventory(clazz, player)

/**
 * Gets the currently open inventory of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @param player The player whose inventory to check
 * @return The open inventory of type T, or null if not found
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getTopInventory instead.",
    ReplaceWith("getTopInventory<T>(player)", "dev.s7a.ktinventory.util.getTopInventory"),
)
inline fun <reified T : AbstractKtInventory> getOpenInventory(player: HumanEntity) = getTopInventory<T>(player)

/**
 * Gets the currently open paginated inventory entry of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param clazz The KClass of the inventory type
 * @param player The player whose inventory to check
 * @return The open paginated inventory entry of type T, or null if not found
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getTopInventoryPaginatedEntry instead.",
    ReplaceWith("getTopInventoryPaginatedEntry(clazz, player)", "dev.s7a.ktinventory.util.getTopInventoryPaginatedEntry"),
)
fun <T : AbstractKtInventoryPaginated<*>> getOpenInventoryPaginated(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginated.Entry<T>? = getTopInventoryPaginatedEntry(clazz, player)

/**
 * Gets the currently open paginated inventory entry of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param player The player whose inventory to check
 * @return The open paginated inventory entry of type T, or null if not found
 * @since 2.0.0
 */
@Deprecated(
    "Deprecated in v2.2.0. Will be removed in v2.5.0. Use getTopInventoryPaginatedEntry instead.",
    ReplaceWith("getTopInventoryPaginatedEntry<T>(player)", "dev.s7a.ktinventory.util.getTopInventoryPaginatedEntry"),
)
inline fun <reified T : AbstractKtInventoryPaginated<*>> getOpenInventoryPaginated(player: HumanEntity) =
    getTopInventoryPaginatedEntry<T>(player)
