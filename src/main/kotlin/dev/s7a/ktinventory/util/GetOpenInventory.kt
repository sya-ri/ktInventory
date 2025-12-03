package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventory
import dev.s7a.ktinventory.AbstractKtInventoryPaginated
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
 * @return The top inventory
 * @throws RuntimeException if the reflection operation fails
 * @since 2.0.0
 */
private fun getTopInventory(viewer: HumanEntity) =
    try {
        val view = viewer.openInventory
        val getTopInventory = view.javaClass.getMethod("getTopInventory")
        getTopInventory.setAccessible(true)
        getTopInventory.invoke(view) as Inventory
    } catch (e: Throwable) {
        throw RuntimeException(e)
    }

/**
 * Gets the currently open inventory of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @param clazz The KClass of the inventory type
 * @param player The player whose inventory to check
 * @return The open inventory of type T, or null if not found
 * @since 2.0.0
 */
fun <T : AbstractKtInventory> getOpenInventory(
    clazz: KClass<T>,
    player: HumanEntity,
): T? = clazz.safeCast(getTopInventory(player).holder)

/**
 * Gets the currently open inventory of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventory]
 * @param player The player whose inventory to check
 * @return The open inventory of type T, or null if not found
 * @since 2.0.0
 */
inline fun <reified T : AbstractKtInventory> getOpenInventory(player: HumanEntity) = getOpenInventory(T::class, player)

/**
 * Gets the currently open paginated inventory entry of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param clazz The KClass of the inventory type
 * @param player The player whose inventory to check
 * @return The open paginated inventory entry of type T, or null if not found
 * @since 2.0.0
 */
@Suppress("UNCHECKED_CAST")
fun <T : AbstractKtInventoryPaginated<*>> getOpenInventoryPaginated(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginated.Entry<T>? {
    val inventory = getTopInventory(player).holder as? AbstractKtInventoryPaginated.Entry<*> ?: return null
    if (clazz.isInstance(inventory.paginated)) return null
    return inventory as AbstractKtInventoryPaginated.Entry<T>
}

/**
 * Gets the currently open paginated inventory entry of the specified type for a player.
 *
 * @param T The type of inventory extending [AbstractKtInventoryPaginated]
 * @param player The player whose inventory to check
 * @return The open paginated inventory entry of type T, or null if not found
 * @since 2.0.0
 */
inline fun <reified T : AbstractKtInventoryPaginated<*>> getOpenInventoryPaginated(player: HumanEntity) =
    getOpenInventoryPaginated(T::class, player)
