package dev.s7a.ktinventory.soundchecker

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Main : JavaPlugin() {
    override fun onEnable() {
        SoundCheckerCommand(this).register()
    }
}
