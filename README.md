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
    implementation("dev.s7a:ktInventory:2.2.0")
}
```

## Usage

Upgrading from v2.1.1 with a custom `KtInventoryPluginContext` implementation requires
adding `handlerId` and recompiling. See the [v2.2.0 migration guide](DEPRECATION.md#required-migration-in-v220).

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

### Choosing a multi-page inventory class

ktInventory has four multi-page inventory patterns. Start with `KtInventoryPaginated`
unless your data source needs one of the more specialized models.

| String-title class | Adventure-title class | Use when | Recommended for |
|--------------------|-----------------------|----------|-----------------|
| `KtInventoryPaginated` | `KtInventoryPaginatedAdventure` | You can build all entries up front as a `List` or collection. | Most static or small-to-medium menus. This is the simplest and most recommended default. |
| `KtInventoryPaginatedSequence` | `KtInventoryPaginatedSequenceAdventure` | Entries are produced lazily as a `Sequence` and you do not need `lastPage` in the title. | Large generated lists where calculating everything immediately is unnecessary. |
| `KtInventoryPaginatedFetched` | `KtInventoryPaginatedFetchedAdventure` | Each page is loaded from a condition such as an offset, cursor, filter, or search key. | Database/API pagination where the data source decides previous and next page conditions. |
| `KtInventoryPaginatedLazyFetched` | `KtInventoryPaginatedLazyFetchedAdventure` | The inventory should open immediately while page data loads asynchronously. | Slow database/API calls. Use `KtInventoryPluginContext.LazyFetchable`; keep Bukkit API work out of `fetch`. |

## Skill

This repository includes an installable skill at `skills/ktinventory`.

### Install with `gh skill`

Install from the repository:

```bash
gh skill install sya-ri/ktInventory ktinventory
```

### Install with `npx skills`

Install from GitHub:

```bash
npx skills add sya-ri/ktInventory --skill ktinventory
```

After installing the skill, restart your agent tooling so it picks up the new metadata.
