# Deprecation List

This document lists features and APIs that have been deprecated in `ktInventory`.

## Constructors

### Base class `Plugin` constructors

- **Deprecated in**: v2.1.0
- **Scheduled for removal**: v2.4.0
- **Replacement**: Base class constructors that accept `KtInventoryPluginContext`

The direct `Plugin` constructors were replaced by `KtInventoryPluginContext` constructors to keep plugin-dependent data in a single context object.
Callers do not need to wrap a plugin instance manually. If an inventory definition wants to accept
`Plugin` for convenient construction, keep that bridge on the inventory class and convert to
`KtInventoryPluginContext` there.

Affected constructors:

- `AbstractKtInventoryPaginated(plugin, line)`
- `KtInventory(plugin, line, altColorChar)`
- `KtInventoryAdventure(plugin, line)`
- `KtInventoryPaginated(plugin, line, altColorChar)`
- `KtInventoryPaginatedAdventure(plugin, line)`

#### Example Migration

```kotlin
// Deprecated
class MainMenu(
    plugin: Plugin,
) : KtInventory(plugin, 3)

// Recommended inventory definition
class MainMenu(
    context: KtInventoryPluginContext,
) : KtInventory(context, 3) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))
}
```

Callers can continue to pass either an injected context or a plugin instance to the inventory:

```kotlin
MainMenu(context).open(player)
MainMenu(plugin).open(player)
```
