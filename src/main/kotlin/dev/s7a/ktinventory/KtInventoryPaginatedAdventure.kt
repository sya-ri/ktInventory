package dev.s7a.ktinventory

import net.kyori.adventure.text.Component
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

abstract class KtInventoryPaginatedAdventure(
    private val plugin: Plugin,
    line: Int,
) : AbstractKtInventoryPaginated<KtInventoryPaginatedAdventure>(plugin, line) {
    abstract fun title(
        page: Int,
        lastPage: Int,
    ): Component

    override fun createEntry(
        page: Int,
        lastPage: Int,
    ): Entry<KtInventoryPaginatedAdventure> = Entry(this, page, lastPage)

    class Entry<T : KtInventoryPaginatedAdventure>(
        paginated: T,
        page: Int,
        lastPage: Int,
    ) : AbstractKtInventoryPaginated.Entry<T>(paginated, page, lastPage) {
        private val _inventory by lazy {
            paginated.plugin.server.createInventory(this, size, paginated.title(page, lastPage))
        }

        override fun getInventory() = _inventory
    }

    abstract class Refreshable<T : KtInventoryPaginatedAdventure>(
        clazz: KClass<T>,
    ) : AbstractKtInventoryPaginated.Refreshable<T>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventoryPaginated.Entry<T>,
        ): T?
    }
}
