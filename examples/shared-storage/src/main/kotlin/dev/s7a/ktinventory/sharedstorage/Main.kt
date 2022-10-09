package dev.s7a.ktinventory.sharedstorage

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Main : JavaPlugin() {
    override fun onEnable() {
        val config = StorageConfig(this)
        StorageCommand(this, config).register()
    }
}
