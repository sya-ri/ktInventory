# ktInventory

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/dev.s7a/ktInventory)](https://search.maven.org/artifact/dev.s7a/ktInventory)
[![KDoc link](https://img.shields.io/badge/API_reference-KDoc-blue)](https://gh.s7a.dev/ktInventory)
[![Build status](https://img.shields.io/github/actions/workflow/status/sya-ri/ktInventory/build.yml?branch=master&label=Test&logo=github)](.github/workflows/build.yml)

Spigot library with Kotlin for easy inventory creation and event handling

## Installation

### build.gradle.kts

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.s7a:ktInventory:1.0.0")
}
```

## Usage

### DSL

You can easily generate inventory. Basically, use this.

```kotlin
fun openSimpleMenu(plugin: JavaPlugin, player: HumanEntity) {
    plugin.ktInventory("&0&lSelect where to teleport", 1) {
        item(3, ItemStack(Material.RED_BED)) {
            val player = it.whoClicked as? Player ?: return@item
            val respawnLocation = player.respawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found respawnLocation")
            }
            player.closeInventory()
        }
        item(5, ItemStack(Material.COMPASS)) {
            val player = it.whoClicked as? Player ?: return@item
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }.open(player)
}
```

### Provider

Passing a plugin instance as a property makes the dependencies between classes a little complicated.
In such cases you can use Provider instead of DSL.

```kotlin
class SimpleMenu(provider: KtInventoryProvider) {
    private val inventory = provider.create("&0&lSelect where to teleport", 1) {
        item(3, ItemStack(Material.RED_BED)) {
            val player = it.whoClicked as? Player ?: return@item
            val respawnLocation = player.respawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found respawnLocation")
            }
            player.closeInventory()
        }
        item(5, ItemStack(Material.COMPASS)) {
            val player = it.whoClicked as? Player ?: return@item
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }

    fun open(player: HumanEntity) {
        inventory.open(player)
    }
}
```
