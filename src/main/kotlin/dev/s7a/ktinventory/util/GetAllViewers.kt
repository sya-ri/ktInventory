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

inline fun <reified T : KtInventory> getAllViewers(): List<Player> =
    Bukkit.getOnlinePlayers().filter {
        getTopInventory(it).holder is T
    }

inline fun <reified T : KtInventoryPaginated> getAllViewers(page: Int? = null): List<Player> =
    Bukkit.getOnlinePlayers().filter {
        val entry = getTopInventory(it).holder as? KtInventoryPaginated.Entry ?: return@filter false
        entry.paginated is T && (page?.let { it == entry.page } != false)
    }
