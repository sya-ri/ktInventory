package dev.s7a.ktinventory.complexmenu

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Main : JavaPlugin() {
    override fun onEnable() {
        val menu = ComplexMenu(this)
        ComplexMenuCommand(this, menu).register()
    }
}
