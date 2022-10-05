import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

allprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    dependencies {
        if (project.hasProperty("USE_SPIGOT_8")) {
            compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
        } else {
            compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

group = "dev.s7a"
version = "1.0.0-SNAPSHOT"
