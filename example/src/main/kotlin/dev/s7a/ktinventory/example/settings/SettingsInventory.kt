package dev.s7a.ktinventory.example.settings

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.KtInventoryPluginContext
import dev.s7a.ktinventory.example.itemStack
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin

class SettingsInventory(
    val context: KtInventoryPluginContext,
) : KtInventory(context, 1) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override fun title() = "&0&lSettings"

    init {
        button(4, itemStack(Material.SUGAR, displayName = "&6Current: $count")) {
            if (it.click.isLeftClick) {
                count += 1
            } else {
                count -= 1
            }
            refreshAll()
        }
    }

    companion object : Refreshable<SettingsInventory>(SettingsInventory::class) {
        private var count = 0

        override fun createNew(
            player: HumanEntity,
            inventory: SettingsInventory,
        ) = SettingsInventory(inventory.context)
    }
}
