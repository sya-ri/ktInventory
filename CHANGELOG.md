# Changelog

## v2.2.0

### Added

- Add generic top inventory holder lookup APIs.
  - These APIs replace the older `AbstractKtInventory`-bound lookup surface with a generic top inventory holder lookup.
  - They can be used for `AbstractKtInventory`, paginated entries, and custom top inventory holder types.
  - `getTopInventory<T>()`
  - `getTopInventory(clazz, player)`
- Add paginated top inventory lookup APIs.
  - These APIs make paginated entry lookup explicit when page state such as the current page is needed.
  - `getTopInventoryPaginatedEntry<T>()`
  - `getTopInventoryPaginatedEntry(clazz, player)`
  - `getTopInventoryPaginatedSequenceEntry<T>()`
  - `getTopInventoryPaginatedSequenceEntry(clazz, player)`
  - `getTopInventoryPaginatedFetchedEntry<T>()`
  - `getTopInventoryPaginatedFetchedEntry(clazz, player)`
  - `getTopInventoryPaginatedLazyFetchedEntry<T>()`
  - `getTopInventoryPaginatedLazyFetchedEntry(clazz, player)`
- Add generic viewer lookup APIs.
  - These APIs generalize viewer lookup beyond `AbstractKtInventory`, matching the new top inventory lookup behavior.
  - The paginated variants provide paginated entry lookup.
  - `getViewers<T>()`
  - `getViewers(clazz)`
  - `getViewersPaginatedEntry<T>()`
  - `getViewersPaginatedEntry(clazz)`
  - `getViewersPaginatedSequenceEntry<T>()`
  - `getViewersPaginatedSequenceEntry(clazz)`
  - `getViewersPaginatedFetchedEntry<T>()`
  - `getViewersPaginatedFetchedEntry(clazz)`
  - `getViewersPaginatedLazyFetchedEntry<T>()`
  - `getViewersPaginatedLazyFetchedEntry(clazz)`
  - `getViewersDeeply<T>()`
- Add sequence-backed paginated inventory base classes.
  - `KtInventoryPaginatedSequence` for legacy string titles.
  - `KtInventoryPaginatedSequenceAdventure` for Adventure `Component` titles.
  - These classes use `entries: Sequence<KtInventoryButton<Entry<T>>>` and intentionally do not expose `lastPage`.
- Add condition-based fetched inventory base classes.
  - `KtInventoryPaginatedFetched` for legacy string titles.
  - `KtInventoryPaginatedFetchedAdventure` for Adventure `Component` titles.
  - These classes fetch entries with `fetch(condition, limit)`, allowing offset, cursor, or filter-based pagination without storing all entries up front.
- Add lazy fetched inventory base classes.
  - `KtInventoryPaginatedLazyFetched` for legacy string titles.
  - `KtInventoryPaginatedLazyFetchedAdventure` for Adventure `Component` titles.
  - These classes open the inventory immediately, run `fetch(condition, limit)` asynchronously through `KtInventoryPluginContext.LazyFetchable`, and place buttons on the server main thread after data is loaded.
- Add refreshable support to condition-fetched and lazy-fetched paginated inventories.
  - `RefreshBehavior.Keep` reopens the current display condition.
  - `RefreshBehavior.OpenFirst` reopens the inventory from `initialCondition`.
- Add entry-aware storables to multi-page inventory base classes.
  - Paginated and sequence-backed inventories can initialize and save storable contents per page entry.
  - Fetched and lazy fetched inventories can initialize and save storable contents per condition entry.
- Add `KtInventoryPluginContext.LazyFetchable` for lazy fetched inventories.
  - Use `KtInventoryPluginContext.LazyFetchable(plugin)` when constructing lazy fetched inventories.
  - `KtInventoryPluginContext(plugin)` remains the scheduler-free context for existing inventory classes.
- Add `KtInventoryPluginContext.handlerId` and `KtInventoryHandlerId` for internal event handler sharing.
  - Contexts created with `KtInventoryPluginContext(plugin)` share the same handler id per plugin instance.
  - Custom context implementations should use `KtInventoryHandlerId.of(plugin)` with the plugin that registers events.
  - This keeps listener registration stable when multiple context wrappers are created for the same plugin.

### Fixed

- Fixed buttons can no longer use pagination slots in paginated inventories.
- Fixed buttons can no longer use storable slots.
- Only the owning plugin's handler processes inventory events and saves storable contents, preventing duplicate callbacks when multiple plugins use ktInventory.
- Disabling a plugin closes only its own inventories and leaves other plugins' menus open.
- Fix plugin-disable cleanup for custom `KtInventoryPluginContext` implementations.
  - Cleanup now matches the context's plugin-associated handler id instead of comparing the context object directly with the plugin.
- `getTopInventory` now returns `null` when the viewer has no available top inventory.
  - This prevents lookup calls from throwing when an inventory view has already been closed or the platform returns no top inventory.
- `getViewersDeeply<T>()` now searches from `KtInventoryBase` holders and resolves paginated entries through their `paginated` inventory.
  - The old deep viewer search was based on `KtInventory`, so it only supported some inventory implementations.
  - It also did not handle paginated inventories correctly because the opened holder is the paginated entry, while the target parent type is attached to the paginated inventory itself.
  - Sequence-backed paginated entries are resolved the same way.

### Changed

- Custom `KtInventoryPluginContext` implementations must add `override val handlerId = KtInventoryHandlerId.of(plugin)` and be recompiled against v2.2.0.
  - This is a source and binary compatibility change for custom implementations. See [the migration guide](DEPRECATION.md#required-migration-in-v220).
  - The built-in `KtInventoryPluginContext(plugin)` factory requires no caller changes.
- Internal refresh logic now uses the new `getTopInventory` and `getViewers` APIs.
- `getTopInventory<T>()` now resolves paginated inventories directly when the open holder is a paginated entry.

### Deprecated

- Deprecate `getOpenInventory` in favor of `getTopInventory`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getOpenInventoryPaginated` in favor of `getTopInventoryPaginatedEntry`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getAllViewers` in favor of `getViewers`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getAllViewersPaginated` in favor of `getViewersPaginatedEntry`.
  - Scheduled for removal in v2.5.0.
- Deprecate `getAllViewersDeeply` in favor of `getViewersDeeply` with `DeprecationLevel.WARNING`.
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
