package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.KtInventoryBase
import org.bukkit.Bukkit
import org.bukkit.entity.Player

inline fun <reified T : KtInventoryBase> T.getAllViewers(): List<Player> =
    Bukkit.getOnlinePlayers().filter {
        it.openInventory.topInventory is T
    }
