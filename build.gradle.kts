import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kover)
    alias(libs.plugins.pluginYml.bukkit) apply false
    alias(libs.plugins.minecraftServer) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.mavenPublish)
}

group = "dev.s7a"
version = "2.2.0"

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
    testImplementation(libs.paper.mockbukkit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockbukkit)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.compileTestKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("dev.s7a", "ktInventory", version.toString())
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationJavadoc"),
            sourcesJar = true,
        ),
    )
    pom {
        name.set("ktInventory")
        description.set("Spigot inventory library for Kotlin. Easy to create and handle")
        inceptionYear.set("2026")
        url.set("https://github.com/sya-ri/ktInventory")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
