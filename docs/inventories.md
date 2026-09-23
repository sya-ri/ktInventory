# Inventory variants

Start with [installation and a basic menu](../README.md). This guide covers Adventure titles and multi-page menus.

## For paper servers

Use `KtInventoryAdventure` to supply an Adventure `Component` title; button setup follows the [basic menu example](../README.md#usage).

```kotlin
class AdventureMenu(context: KtInventoryPluginContext) : KtInventoryAdventure(context, 1) {
    override fun title() = Component.text("Select where to teleport")
}
```

## Choosing a multi-page inventory class

ktInventory has four multi-page inventory patterns. Start with `KtInventoryPaginated`
unless your data source needs one of the more specialized models.

| String-title class | Adventure-title class | Use when |
| --- | --- | --- |
| `KtInventoryPaginated` | `KtInventoryPaginatedAdventure` | All entries are available as a list or collection. |
| `KtInventoryPaginatedSequence` | `KtInventoryPaginatedSequenceAdventure` | Entries are generated lazily and the title does not need `lastPage`. |
| `KtInventoryPaginatedFetched` | `KtInventoryPaginatedFetchedAdventure` | A data source supplies each page using offsets, cursors, or filters. |
| `KtInventoryPaginatedLazyFetched` | `KtInventoryPaginatedLazyFetchedAdventure` | Pages load asynchronously after opening. Use `KtInventoryPluginContext.LazyFetchable` and keep Bukkit API calls out of `fetch`. |
