import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

configure<BukkitPluginDescription> {
    commands {
        register("sound-checker") {
            permission = "sound-checker.use"
        }
    }
}
