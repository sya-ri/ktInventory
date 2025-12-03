package dev.s7a.ktinventory


/**
 * Interface for objects that have a parent inventory.
 *
 * @param T The type of parent inventory
 * @since 2.0.0
 */
interface HasParentInventory<T : ParentInventory> {
    /**
     * The parent inventory of this object, or null if it has no parent.
     *
     * @since 2.0.0
     */
    val parentInventory: T?
}
