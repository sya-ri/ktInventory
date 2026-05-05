# ktInventory

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
    implementation("dev.s7a:ktInventory:2.1.0")
}
```

## Usage

### For spigot servers

```kotlin
class SimpleMenu(
    context: KtInventoryPluginContext,
) : KtInventory(context, 1) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override fun title() = "&0&lSelect where to teleport"

    init {
        button(3, ItemStack(Material.RED_BED)) { event, _ ->
            val player = event.whoClicked as? Player ?: return@button
            val respawnLocation = player.respawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found respawnLocation")
            }
            player.closeInventory()
        }
        button(5, ItemStack(Material.COMPASS)) { event, _ ->
            val player = event.whoClicked as? Player ?: return@button
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }
}
```

### For paper servers

```kotlin
class SimpleMenu(
    context: KtInventoryPluginContext,
) : KtInventoryAdventure(context, 1) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override fun title() = Component.text("Select where to teleport").color(NamedTextColor.BLACK).decorate(TextDecoration.BOLD)

    init {
        button(3, ItemStack(Material.RED_BED)) { event, _ ->
            val player = event.whoClicked as? Player ?: return@button
            val respawnLocation = player.respawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found respawnLocation")
            }
            player.closeInventory()
        }
        button(5, ItemStack(Material.COMPASS)) { event, _ ->
            val player = event.whoClicked as? Player ?: return@button
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }
}
```

## Codex Skill

This repository includes an installable Codex skill at `skills/ktinventory`.

### Install with `gh skill`

Install from the repository and target Codex explicitly:

```bash
gh skill install sya-ri/ktInventory ktinventory --agent codex
```

Install from the local checkout instead:

```bash
gh skill install ./skills/ktinventory --agent codex
```

### Install with `npx skills`

Install from GitHub:

```bash
npx skills add sya-ri/ktInventory --skill ktinventory -a codex
```

Install from the direct skill path:

```bash
npx skills add https://github.com/sya-ri/ktInventory/tree/master/skills/ktinventory -a codex
```

List installable skills in this repository:

```bash
npx skills add sya-ri/ktInventory --list
```

After installing the skill, restart Codex so it picks up the new metadata.
