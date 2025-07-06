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

fun <T : AbstractKtInventory> getOpenInventory(
    clazz: KClass<T>,
    player: HumanEntity,
): T? = clazz.safeCast(getTopInventory(player).holder)

inline fun <reified T : AbstractKtInventory> getOpenInventory(player: HumanEntity) = getOpenInventory(T::class, player)

@Suppress("UNCHECKED_CAST")
fun <T : AbstractKtInventoryPaginated<*>> getOpenInventoryPaginated(
    clazz: KClass<T>,
    player: HumanEntity,
): AbstractKtInventoryPaginated.Entry<T>? {
    val inventory = getTopInventory(player).holder as? AbstractKtInventoryPaginated.Entry<*> ?: return null
    if (clazz.isInstance(inventory.paginated)) return null
    return inventory as AbstractKtInventoryPaginated.Entry<T>
}

inline fun <reified T : AbstractKtInventoryPaginated<*>> getOpenInventoryPaginated(player: HumanEntity) =
    getOpenInventoryPaginated(T::class, player)
