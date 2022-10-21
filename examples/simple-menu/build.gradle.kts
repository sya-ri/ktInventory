import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

configure<BukkitPluginDescription> {
    commands {
        register("simple-menu") {
            permission = "simple-menu.use"
        }
    }
}
