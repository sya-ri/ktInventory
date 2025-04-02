package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPaginated
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.reflect.KClass

fun <T : KtInventory> getAllViewers(clazz: KClass<T>): Map<Player, T> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getOpenInventory(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

inline fun <reified T : KtInventory> getAllViewers() = getAllViewers(T::class)

fun <T : KtInventoryPaginated> getAllViewersPaginated(clazz: KClass<T>): Map<Player, KtInventoryPaginated.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getOpenInventoryPaginated(clazz, player) ?: return@mapNotNull null
            player to (inventory)
        }.toMap()

inline fun <reified T : KtInventoryPaginated> getAllViewersPaginated() = getAllViewersPaginated(T::class)
