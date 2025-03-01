import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

configure<BukkitPluginDescription> {
    commands {
        register("complex-menu") {
            permission = "complex-menu.use"
        }
    }
}
