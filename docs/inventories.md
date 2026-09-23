# Inventory variants

Start with [installation and a basic menu](../README.md). This guide covers Adventure titles and multi-page menus.

## For paper servers

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

## Choosing a multi-page inventory class

ktInventory has four multi-page inventory patterns. Start with `KtInventoryPaginated`
unless your data source needs one of the more specialized models.

| String-title class | Adventure-title class | Use when | Recommended for |
|--------------------|-----------------------|----------|-----------------|
| `KtInventoryPaginated` | `KtInventoryPaginatedAdventure` | You can build all entries up front as a `List` or collection. | Most static or small-to-medium menus. This is the simplest and most recommended default. |
| `KtInventoryPaginatedSequence` | `KtInventoryPaginatedSequenceAdventure` | Entries are produced lazily as a `Sequence` and you do not need `lastPage` in the title. | Large generated lists where calculating everything immediately is unnecessary. |
| `KtInventoryPaginatedFetched` | `KtInventoryPaginatedFetchedAdventure` | Each page is loaded from a condition such as an offset, cursor, filter, or search key. | Database/API pagination where the data source decides previous and next page conditions. |
| `KtInventoryPaginatedLazyFetched` | `KtInventoryPaginatedLazyFetchedAdventure` | The inventory should open immediately while page data loads asynchronously. | Slow database/API calls. Use `KtInventoryPluginContext.LazyFetchable`; keep Bukkit API work out of `fetch`. |
