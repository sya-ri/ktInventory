# ktInventory Patterns

## Pick The Right Base Class

- `KtInventory`
  Use for Spigot-style inventories with `String` titles and legacy `&` color codes.
- `KtInventoryAdventure`
  Use on Paper when the title should be an Adventure `Component`.
- `KtInventoryPaginated`
  Use when the menu is a list split across multiple pages.
- `KtInventoryPaginatedAdventure`
  Use when you need both pagination and an Adventure title.

## Minimal Menu

Use this shape for a normal one-page menu:

```kotlin
class SimpleMenu(
    context: KtInventoryPluginContext,
) : KtInventory(context, 1) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override fun title() = "&0&lSelect"

    init {
        button(3, itemStack(Material.RED_BED, "&cRespawn")) { event ->
            val player = event.player as? Player ?: return@button
            player.closeInventory()
        }
    }
}
```

Callers should pass an injected `KtInventoryPluginContext` directly. If they have a plugin
instance, call `SimpleMenu(plugin)` and keep the conversion inside the inventory definition.

## Paper Title Pattern

Use `KtInventoryAdventure` when the title should be a `Component`:

```kotlin
override fun title() =
    Component.text("Select")
        .color(NamedTextColor.BLACK)
        .decorate(TextDecoration.BOLD)
```

## Refreshable Inventory Pattern

Use a companion `Refreshable` when the screen should reopen with updated state:

```kotlin
companion object : Refreshable<SettingsInventory>(SettingsInventory::class) {
    override fun createNew(
        player: HumanEntity,
        inventory: SettingsInventory,
    ) = SettingsInventory(inventory.context)
}
```

Typical flow:

- mutate shared state in a click handler
- call `refresh(player, inventory)` or `refreshAll()`
- rebuild the menu in `createNew(...)`

## Paginated Inventory Pattern

Use a paginated base class for large lists:

```kotlin
class SoundCheckInventory(
    context: KtInventoryPluginContext,
) : KtInventoryPaginated(context, 6) {
    override val entries =
        Registry.SOUNDS.map { sound ->
            createButton(itemStack(Material.GRAY_DYE, "&6${sound.key.key}")) { event ->
                val player = event.player as? Player ?: return@createButton
                player.playSound(player.location, sound, 1F, 1F)
            }
        }

    override fun title(page: Int, lastPage: Int) =
        "&0&lSound checker (${page + 1}/${lastPage + 1})"

    init {
        paginateSlot(0 until 45)
        previousPageButton(45, itemStack(Material.ARROW, "&d<<"))
        nextPageButton(53, itemStack(Material.ARROW, "&d>>"))
    }
}
```

Rules of thumb:

- reserve a contiguous content area with `paginateSlot(...)`
- keep navigation buttons outside that area
- keep fixed buttons, pagination slots, and paged storable slots separate
- build each row entry with `createButton(...)`

Use `KtInventorySequence` when the entry source is lazy or sequence-backed. Sequence-backed titles receive only the current page:

```kotlin
class SoundCheckInventory(
    context: KtInventoryPluginContext,
) : KtInventorySequence(context, 6) {
    constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))

    override val entries =
        Registry.SOUNDS.asSequence().map { sound ->
            createButton(itemStack(Material.GRAY_DYE, "&6${sound.key.key}")) { event ->
                val player = event.player as? Player ?: return@createButton
                player.playSound(player.location, sound, 1F, 1F)
            }
        }

    override fun title(page: Int) =
        "&0&lSound checker (${page + 1})"

    init {
        paginateSlot(0 until 45)
        previousPageButton(45, itemStack(Material.ARROW, "&d<<"))
        nextPageButton(53, itemStack(Material.ARROW, "&d>>"))
    }
}
```

Use `KtInventoryFetched` when the data source should decide page boundaries with an offset, cursor, filter, or other condition:

```kotlin
class ItemBrowserInventory(
    context: KtInventoryPluginContext,
) : KtInventoryFetched<String>(context, 6) {
    override val initialCondition = "start"

    override fun fetch(
        condition: String,
        limit: Int,
    ): Page<String, KtInventoryButton<Entry<String>>> {
        val page = repository.fetchItems(cursor = condition, limit = limit)
        return Page(
            entries = page.items.map { item ->
                createButton(itemStack(Material.PAPER, item.name)) {}
            },
            previousCondition = page.previousCursor,
            nextCondition = page.nextCursor,
        )
    }

    override fun title(condition: String) = "&0&lItems"

    init {
        paginateSlot(0 until 45)
        previousPageButton(45, itemStack(Material.ARROW, "&d<<"))
        nextPageButton(53, itemStack(Material.ARROW, "&d>>"))
    }
}
```

Use `KtInventoryLazyFetched` with `KtInventoryPluginContext.LazyFetchable` when the inventory should open immediately and fetch data asynchronously. Keep Bukkit API usage out of `fetch`; create item buttons in `createButton`, which runs on the server main thread.

## Storable Slots Pattern

Use `storable(...)` when players should place or move items inside managed slots:

```kotlin
storable(
    slots = listOf(10, 11, 12),
    initialize = { savedItems },
    save = { items -> saveItems(items) },
)
```

Keep persistence inside `save` so the inventory class stays focused on UI behavior.

For paginated, sequence-backed, fetched, or lazy fetched inventories, define editable slots on the parent inventory. The callback receiver is the current entry, so use `page` or `condition` directly:

```kotlin
class EditablePagesInventory(
    context: KtInventoryPluginContext,
) : KtInventoryPaginated(context, 6) {
    override val entries = entriesFromRepository()

    override fun title(page: Int, lastPage: Int) =
        "&0&lEditable ${page + 1}/${lastPage + 1}"

    init {
        paginateSlot(9 until 45)
        storable(
            slots = 0 until 9,
            initialize = { loadItemsForPage(page) },
            save = { items -> saveItemsForPage(page, items) },
        )
    }
}
```

Paged storable slots must not overlap pagination slots or fixed button slots.

## Custom Context Pattern

When implementing `KtInventoryPluginContext` yourself, use a handler id derived from the plugin that registers events:

```kotlin
class MyInventoryContext(
    private val plugin: Plugin,
) : KtInventoryPluginContext {
    override val handlerId = KtInventoryHandlerId.of(plugin)

    override fun registerEvents(listener: Listener) {
        plugin.server.pluginManager.registerEvents(listener, plugin)
    }
}
```

## Viewer And Top Inventory Lookup Pattern

Use `getTopInventory` when checking what a single player has open:

```kotlin
val menu = getTopInventory<SettingsInventory>(player) ?: return
```

For paginated inventories, choose the lookup by the value you need:

```kotlin
// Paginated inventory instance
val inventory = getTopInventoryPaginated<SoundCheckInventory>(player)

// Current page entry, including page state
val entry = getTopInventoryPaginatedEntry<SoundCheckInventory>(player)
```

Use `getViewers` for viewer maps:

```kotlin
val viewers = getViewers<SettingsInventory>()
val paginated = getViewersPaginated<SoundCheckInventory>()
val paginatedEntries = getViewersPaginatedEntry<SoundCheckInventory>()
```

Use `getViewersDeeply<ParentInventoryType>()` when child inventories or paginated entries should be associated with a parent inventory.

Deprecated lookup aliases should not be used in new code:

- `getOpenInventory`
- `getOpenInventoryPaginated`
- `getAllViewers`
- `getAllViewersPaginated`
- `getAllViewersDeeply`

## Verification

- General compile check: `./gradlew build`
- Live plugin verification on Paper example server:
  - `./gradlew :example:testPlugin21`
