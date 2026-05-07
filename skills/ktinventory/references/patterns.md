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
- build each row entry with `createButton(...)`

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

## Verification

- General compile check: `./gradlew build`
- Live plugin verification on Paper example server:
  - `./gradlew :example:testPlugin21`
