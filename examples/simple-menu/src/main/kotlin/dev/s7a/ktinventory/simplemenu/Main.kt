package dev.s7a.ktinventory.simplemenu

import dev.s7a.ktinventory.KtInventoryProvider
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Main : JavaPlugin() {
    override fun onEnable() {
        val provider = KtInventoryProvider(this)
        val menu = SimpleMenu(provider)
        SimpleMenuCommand(this, menu).register()
    }
}
