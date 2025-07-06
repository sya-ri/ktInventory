package dev.s7a.ktinventory.util

import dev.s7a.ktinventory.AbstractKtInventory
import dev.s7a.ktinventory.AbstractKtInventoryPaginated
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.reflect.KClass

fun <T : AbstractKtInventory> getAllViewers(clazz: KClass<T>): Map<Player, T> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getOpenInventory(clazz, player) ?: return@mapNotNull null
            player to inventory
        }.toMap()

inline fun <reified T : AbstractKtInventory> getAllViewers() = getAllViewers(T::class)

fun <T : AbstractKtInventoryPaginated<*>> getAllViewersPaginated(clazz: KClass<T>): Map<Player, AbstractKtInventoryPaginated.Entry<T>> =
    Bukkit
        .getOnlinePlayers()
        .mapNotNull { player ->
            val inventory = getOpenInventoryPaginated(clazz, player) ?: return@mapNotNull null
            player to (inventory)
        }.toMap()

inline fun <reified T : AbstractKtInventoryPaginated<*>> getAllViewersPaginated() = getAllViewersPaginated(T::class)
