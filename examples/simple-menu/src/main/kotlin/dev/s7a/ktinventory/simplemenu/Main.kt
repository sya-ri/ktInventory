package dev.s7a.ktinventory.simplemenu

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Main : JavaPlugin() {
    override fun onEnable() {
        val menu = SimpleMenu(this)
        SimpleMenuCommand(this, menu).register()
    }
}
