package dev.s7a.ktinventory

import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

/**
 * Interface for inventories that can be refreshed.
 *
 * @since 2.0.0
 */
interface RefreshableInventory<T> {
    /**
     * Refreshes the inventory for the specified player based on a given predicate.
     *
     * @param player The player whose inventory should be refreshed
     * @param predicate A function that determines whether an inventory should be refreshed. Returns true to refresh, false to skip.
     * @return True if the refresh was successful, false otherwise
     * @since 2.0.0
     */
    fun refresh(
        player: HumanEntity,
        predicate: (T) -> Boolean = { true },
    ): Boolean

    /**
     * Refreshes the specified inventory for the player.
     *
     * @param player The player whose inventory should be refreshed
     * @param inventory The inventory to refresh
     * @return True if the refresh was successful, false otherwise
     * @since 2.0.0
     */
    fun refresh(
        player: HumanEntity,
        inventory: T,
    )

    /**
     * Refreshes all inventories that match the given predicate.
     *
     * @param predicate A function that determines whether an inventory should be refreshed for a specific player.
     *                 Takes a Player and inventory T as parameters and returns true to refresh, false to skip.
     *                 Default predicate returns true for all cases.
     * @since 2.0.0
     */
    fun refreshAll(predicate: (Player, T) -> Boolean = { _, _ -> true })
}
