import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2" apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.jmailen.kotlinter") version "3.13.0"
    `maven-publish`
    signing
}

group = "dev.s7a"
version = "1.0.0-SNAPSHOT"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jmailen.kotlinter")

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    if (project.hasProperty("USE_SPIGOT_8")) {
        compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    } else {
        compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
    }
    testImplementation(kotlin("test"))
    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:2.145.0")
}

tasks.test {
    useJUnitPlatform()
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven {
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                },
            )
            credentials {
                username = project.properties["credentials.username"].toString()
                password = project.properties["credentials.password"].toString()
            }
        }
    }
    publications {
        register<MavenPublication>("maven") {
            groupId = "dev.s7a"
            artifactId = "ktInventory"
            from(components["kotlin"])
            artifact(sourceJar.get())
            pom {
                name.set("ktInventory")
                description.set("Spigot library for Kotlin for easy inventory creation and event handling")
                url.set("https://github.com/sya-ri/ktInventory")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/sya-ri/ktInventory/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("sya-ri")
                        name.set("sya-ri")
                        email.set("contact@s7a.dev")
                    }
                }
                scm {
                    url.set("https://github.com/sya-ri/ktInventory")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

