package dev.s7a.ktinventory

import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

abstract class KtInventoryPaginated(
    private val plugin: Plugin,
    line: Int,
    private val altColorChar: Char? = '&',
) : AbstractKtInventoryPaginated<KtInventoryPaginated>(plugin, line) {
    abstract fun title(
        page: Int,
        lastPage: Int,
    ): String

    override fun createEntry(
        page: Int,
        lastPage: Int,
    ): Entry<KtInventoryPaginated> = Entry(this, page, lastPage)

    class Entry<T : KtInventoryPaginated>(
        paginated: T,
        page: Int,
        lastPage: Int,
    ) : AbstractKtInventoryPaginated.Entry<T>(paginated, page, lastPage) {
        private val _inventory by lazy {
            paginated.plugin.server.createInventory(
                this,
                size,
                if (paginated.altColorChar != null) {
                    ChatColor.translateAlternateColorCodes(paginated.altColorChar, paginated.title(page, lastPage))
                } else {
                    paginated.title(page, lastPage)
                },
            )
        }

        override fun getInventory() = _inventory
    }

    abstract class Refreshable<T : KtInventoryPaginated>(
        clazz: KClass<T>,
    ) : AbstractKtInventoryPaginated.Refreshable<T>(clazz) {
        abstract override fun createNew(
            player: HumanEntity,
            inventory: AbstractKtInventoryPaginated.Entry<T>,
        ): T?
    }
}
