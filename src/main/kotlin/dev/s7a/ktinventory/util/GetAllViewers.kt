package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPaginated
import org.bukkit.Bukkit
import org.bukkit.entity.Player

inline fun <reified T : KtInventory> getAllViewers(): List<Player> =
    Bukkit.getOnlinePlayers().filter {
        it.openInventory.topInventory.holder is T
    }

inline fun <reified T : KtInventoryPaginated> getAllViewers(page: Int? = null): List<Player> =
    Bukkit.getOnlinePlayers().filter {
        val entry = it.openInventory.topInventory.holder as? KtInventoryPaginated.Entry ?: return@filter false
        entry.paginated is T && (page?.let { it == entry.page } != false)
    }
