import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.pluginYml.bukkit)
    alias(libs.plugins.minecraftServer)
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.spigotLatest)
    implementation(project(":"))
}

configure<BukkitPluginDescription> {
    main = "dev.s7a.ktinventory.example.ExamplePlugin"
    author = "sya_ri"
    version = rootProject.version.toString()
    apiVersion = "1.13"
    commands {
        register("example")
    }
}

tasks.getting(ShadowJar::class) {
    configurations = listOf(project.configurations.getByName("implementation"))
}

listOf(
    "8" to "1.8.8",
    "9" to "1.9.4",
    "10" to "1.10.2",
    "11" to "1.11.2",
    "12" to "1.12.2",
    "13" to "1.13.2",
    "14" to "1.14.4",
    "15" to "1.15.2",
    "16" to "1.16.5",
    "17" to "1.17.1",
    "18" to "1.18.2",
    "19" to "1.19.2",
    "20" to "1.20.4",
    "21" to "1.21.4",
).forEach { (name, version) ->
    task<LaunchMinecraftServerTask>("testPlugin$name") {
        dependsOn("build")

        doFirst {
            copy {
                from(
                    layout.buildDirectory
                        .get()
                        .asFile
                        .resolve("libs/${project.name}-all.jar"),
                )
                into(
                    layout.buildDirectory
                        .get()
                        .asFile
                        .resolve("MinecraftServer$name/plugins"),
                )
            }
        }

        serverDirectory.set(
            layout.buildDirectory
                .get()
                .asFile
                .resolve("MinecraftServer$name")
                .absolutePath,
        )
        jarUrl.set(JarUrl.Paper(version))
        agreeEula.set(true)
    }
}

tasks.build {
    dependsOn("shadowJar")
}
