# Changelog

## v2.2.0-SNAPSHOT

### Added

- Add generic top inventory holder lookup APIs.
  - These APIs replace the older `AbstractKtInventory`-bound lookup surface with a generic top inventory holder lookup.
  - They can be used for `AbstractKtInventory`, paginated entries, and custom top inventory holder types.
  - `getTopInventory<T>()`
  - `getTopInventory(clazz, player)`
- Add paginated top inventory lookup APIs.
  - These APIs make the distinction between a paginated inventory instance and the currently opened page entry explicit.
  - Use `getTopInventoryPaginated` when you need the paginated inventory itself, and `getTopInventoryPaginatedEntry` when you need page state such as the current page.
  - `getTopInventoryPaginated<T>()`
  - `getTopInventoryPaginated(clazz, player)`
  - `getTopInventoryPaginatedEntry<T>()`
  - `getTopInventoryPaginatedEntry(clazz, player)`
  - `getTopInventorySequenceEntry<T>()`
  - `getTopInventorySequenceEntry(clazz, player)`
- Add generic viewer lookup APIs.
  - These APIs generalize viewer lookup beyond `AbstractKtInventory`, matching the new top inventory lookup behavior.
  - The paginated variants provide both paginated inventory lookup and paginated entry lookup.
  - `getViewers<T>()`
  - `getViewers(clazz)`
  - `getViewersPaginated<T>()`
  - `getViewersPaginated(clazz)`
  - `getViewersPaginatedEntry<T>()`
  - `getViewersPaginatedEntry(clazz)`
  - `getViewersSequenceEntry<T>()`
  - `getViewersSequenceEntry(clazz)`
  - `getViewersDeeply<T>()`
- Add sequence-backed paginated inventory base classes.
  - `KtInventorySequence` for legacy string titles.
  - `KtInventorySequenceAdventure` for Adventure `Component` titles.
  - These classes use `entries: Sequence<KtInventoryButton<Entry<T>>>` and intentionally do not expose `lastPage`.
- Add `KtInventoryPluginContext.handlerId` and `KtInventoryHandlerId` for internal event handler sharing.
  - Contexts created with `KtInventoryPluginContext(plugin)` share the same handler id per plugin instance.
  - Custom context implementations should use `KtInventoryHandlerId.of(plugin)` with the plugin that registers events.
  - This keeps listener registration stable when multiple context wrappers are created for the same plugin.

### Fixed

- Fix plugin-disable cleanup for custom `KtInventoryPluginContext` implementations.
  - Custom contexts could previously create handler ids that were not associated with the plugin instance, so inventories were not closed automatically when the plugin was disabled.
- `getTopInventory` now returns `null` when the viewer has no available top inventory.
  - This prevents lookup calls from throwing when an inventory view has already been closed or the platform returns no top inventory.
- `getViewersDeeply<T>()` now searches from `KtInventoryBase` holders and resolves paginated entries through their `paginated` inventory.
  - The old deep viewer search was based on `KtInventory`, so it only supported some inventory implementations.
  - It also did not handle paginated inventories correctly because the opened holder is the paginated entry, while the target parent type is attached to the paginated inventory itself.
  - Sequence-backed paginated entries are resolved the same way.

### Changed

- Internal refresh logic now uses the new `getTopInventory` and `getViewers` APIs.

### Deprecated

- Deprecate `getOpenInventory` in favor of `getTopInventory`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getOpenInventoryPaginated` in favor of `getTopInventoryPaginatedEntry`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getAllViewers` in favor of `getViewers`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getAllViewersPaginated` in favor of `getViewersPaginatedEntry`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getAllViewersDeeply` in favor of `getViewersDeeply` with `DeprecationLevel.ERROR`.
  - Scheduled for removal in v2.5.0.
  - `getAllViewersDeeply` keeps its previous `KtInventory`-based behavior for binary/source compatibility semantics.
  - Migration to `getViewersDeeply` is intentionally a behavior fix, but it is not fully behavior-compatible: the new API searches from `KtInventoryBase` and supports paginated inventories, so the returned viewer map can include inventories that the old API missed.

## v2.1.1

### Added

- Add installable `ktinventory` skill for agent tooling.
  - Includes repository guidance for building Bukkit, Spigot, and Paper inventory UIs with `ktInventory`.
  - Documents recommended class selection, constructor usage, pagination, refresh, and storage patterns.

## v2.1.0

### Added

- Add `KtInventoryPluginContext` to simplify plugin dependency injection.
  - Replaced direct `Plugin` injection in ktInventory base classes with `KtInventoryPluginContext`, which packages plugin-dependent data.
  - This simplifies constructor dependencies and makes testing or future dependency swapping easier.
  - Existing `Plugin` constructors are now `@Deprecated` and delegate to `KtInventoryPluginContext(plugin)`.
    - Scheduled for removal in v2.4.0.
    ```kotlin
    // Before
    class SimpleMenu(
        plugin: Plugin,
    ) : KtInventory(plugin, 1)

    // After
    class SimpleMenu(
        context: KtInventoryPluginContext,
    ) : KtInventory(context, 1) {
        constructor(plugin: Plugin) : this(KtInventoryPluginContext(plugin))
    }
    ```

## v2.0.0

Initial release.
