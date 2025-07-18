import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.pluginYml.bukkit) apply false
    alias(libs.plugins.minecraftServer) apply false
    alias(libs.plugins.shadow) apply false
    `maven-publish`
    signing
}

group = "dev.s7a"
version = "2.0.0-SNAPSHOT"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jmailen.kotlinter")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    tasks.compileJava {
        targetCompatibility = "1.8"
    }

    tasks.compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
}

dependencies {
    compileOnly(libs.paper)
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            url =
                uri(
                    if (version.toString().endsWith("SNAPSHOT")) {
                        "https://central.sonatype.com/repository/maven-snapshots/"
                    } else {
                        "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
                    },
                )
            credentials {
                username = properties["sonatypeUsername"].toString()
                password = properties["sonatypePassword"].toString()
            }
        }
    }
    publications {
        register<MavenPublication>("maven") {
            groupId = "dev.s7a"
            artifactId = "ktInventory"
            from(components["kotlin"])
            artifact(sourceJar.get())
            artifact(javadocJar.get())
            pom {
                name.set("ktInventory")
                description.set("Spigot inventory library for Kotlin. Easy to create and handle")
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
    val key = properties["signingKey"]?.toString()?.replace("\\n", "\n")
    val password = properties["signingPassword"]?.toString()

    useInMemoryPgpKeys(key, password)
    sign(publishing.publications["maven"])
}
