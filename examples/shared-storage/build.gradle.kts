import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

configure<BukkitPluginDescription> {
    commands {
        register("storage") {
            permission = "storage.use"
        }
    }
}
