package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPaginated
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * Get the upper inventory involved in this transaction via reflection.
 *
 * https://www.spigotmc.org/threads/inventoryview-changed-to-interface-backwards-compatibility.651754/#post-4747875
 */
fun getTopInventory(viewer: HumanEntity) =
    try {
        val view = viewer.openInventory
        val getTopInventory = view.javaClass.getMethod("getTopInventory")
        getTopInventory.setAccessible(true)
        getTopInventory.invoke(view) as Inventory
    } catch (e: Throwable) {
        throw RuntimeException(e)
    }

inline fun <reified T : KtInventory> getAllViewers(): Map<Player, T> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventory(player).holder as? T ?: return@mapNotNull null
            player to inventory
        }.toMap()

@Suppress("UNCHECKED_CAST")
inline fun <reified T : KtInventoryPaginated> getAllViewersPaginated(): Map<Player, KtInventoryPaginated.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getTopInventory(player).holder as? KtInventoryPaginated.Entry<*> ?: return@mapNotNull null
            if (inventory.paginated !is T) return@mapNotNull null
            player to (inventory as KtInventoryPaginated.Entry<T>)
        }.toMap()
