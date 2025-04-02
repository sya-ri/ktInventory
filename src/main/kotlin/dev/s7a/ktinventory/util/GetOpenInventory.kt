package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPaginated
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
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

fun <T : KtInventory> getOpenInventory(
    clazz: KClass<T>,
    player: Player,
): T? = clazz.safeCast(getTopInventory(player).holder)

inline fun <reified T : KtInventory> getOpenInventory(player: Player) = getOpenInventory(T::class, player)

@Suppress("UNCHECKED_CAST")
fun <T : KtInventoryPaginated> getOpenInventoryPaginated(
    clazz: KClass<T>,
    player: Player,
): KtInventoryPaginated.Entry<T>? {
    val inventory = getTopInventory(player).holder as? KtInventoryPaginated.Entry<*> ?: return null
    if (clazz.isInstance(inventory.paginated)) return null
    return inventory as KtInventoryPaginated.Entry<T>
}

inline fun <reified T : KtInventoryPaginated> getOpenInventoryPaginated(player: Player) = getOpenInventoryPaginated(T::class, player)
