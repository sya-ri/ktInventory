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

## Inventory Lookup APIs

### `getOpenInventory`

- **Deprecated in**: v2.2.0
- **Scheduled for removal**: v2.5.0
- **Replacement**: `getTopInventory`

`getOpenInventory` was limited to `AbstractKtInventory`. Use `getTopInventory` for generic top inventory holder lookup.

#### Example Migration

```kotlin
// Deprecated
val inventory = getOpenInventory<MyInventory>(player)

// Recommended
val inventory = getTopInventory<MyInventory>(player)
```

### `getOpenInventoryPaginated`

- **Deprecated in**: v2.2.0
- **Scheduled for removal**: v2.5.0
- **Replacement**: `getTopInventoryPaginatedEntry`

`getOpenInventoryPaginated` returns the currently opened paginated entry. Use `getTopInventoryPaginatedEntry` when page state is needed.

#### Example Migration

```kotlin
// Deprecated
val entry = getOpenInventoryPaginated<MyPaginatedInventory>(player)

// Recommended
val entry = getTopInventoryPaginatedEntry<MyPaginatedInventory>(player)
```

## Viewer Lookup APIs

### `getAllViewers`

- **Deprecated in**: v2.2.0
- **Scheduled for removal**: v2.5.0
- **Replacement**: `getViewers`

`getAllViewers` was limited to `AbstractKtInventory`. Use `getViewers` for generic top inventory holder viewer lookup.

#### Example Migration

```kotlin
// Deprecated
val viewers = getAllViewers<MyInventory>()

// Recommended
val viewers = getViewers<MyInventory>()
```

### `getAllViewersPaginated`

- **Deprecated in**: v2.2.0
- **Scheduled for removal**: v2.5.0
- **Replacement**: `getViewersPaginatedEntry`

`getAllViewersPaginated` returns viewers mapped to paginated entries. Use `getViewersPaginatedEntry` for the same entry-based behavior.

#### Example Migration

```kotlin
// Deprecated
val viewers = getAllViewersPaginated<MyPaginatedInventory>()

// Recommended
val viewers = getViewersPaginatedEntry<MyPaginatedInventory>()
```

### `getAllViewersDeeply`

- **Deprecated in**: v2.2.0
- **Scheduled for removal**: v2.5.0
- **Replacement**: `getViewersDeeply`
- **Deprecation level**: `ERROR`

`getAllViewersDeeply` keeps its previous `KtInventory`-based behavior for compatibility, but that behavior only supported some inventory implementations and did not handle paginated inventories correctly. Use `getViewersDeeply`, which searches from `KtInventoryBase` holders and resolves paginated entries through their `paginated` inventory.

This migration is a correctness fix, but it is not fully behavior-compatible. Code that moves from
`getAllViewersDeeply` to `getViewersDeeply` can observe additional viewers because inventories that
were previously skipped, including paginated inventories, are now included.

#### Example Migration

```kotlin
// Deprecated
val viewers = getAllViewersDeeply<MyParentInventory>()

// Recommended
val viewers = getViewersDeeply<MyParentInventory>()
```
