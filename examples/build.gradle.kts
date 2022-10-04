import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2" apply false
    id("dev.s7a.gradle.minecraft.server") version "2.0.0" apply false
}

subprojects {
    apply(plugin = "net.minecrell.plugin-yml.bukkit")
    apply(plugin = "dev.s7a.gradle.minecraft.server")

    dependencies {
        implementation(project(":"))
    }

    configure<BukkitPluginDescription> {
        main = "dev.s7a.ktinventory.${project.name.replace("-", "")}.Main"
        author = "sya_ri"
        version = rootProject.version.toString()
    }

    listOf(
        "16" to "1.16.5",
        "17" to "1.17.1",
        "18" to "1.18.2",
        "19" to "1.19.2"
    ).forEach { (name, version) ->
        task<LaunchMinecraftServerTask>("testPlugin$name") {
            dependsOn("build")

            doFirst {
                copy {
                    from(buildDir.resolve("libs/${project.name}.jar"))
                    into(buildDir.resolve("MinecraftServer$name/plugins"))
                }
            }

            serverDirectory.set(buildDir.resolve("MinecraftServer$name"))
            jarUrl.set(JarUrl.Paper(version))
            agreeEula.set(true)
        }
    }
}
