---
name: ktinventory
description: Use when building or updating Bukkit, Spigot, or Paper inventory GUIs with dev.s7a:ktInventory, especially for menus, paginated inventories, refreshable inventories, storable slots, or Adventure title support.
---

# ktInventory

Use this skill when the task is about inventory UIs built with `ktInventory`.

## Workflow

1. Confirm the server flavor and choose the base class that matches it:
   - `KtInventory`: string title with legacy color codes
   - `KtInventoryAdventure`: Adventure `Component` title
   - `KtInventoryPaginated`: paginated string-title inventory
   - `KtInventoryPaginatedAdventure`: paginated Adventure-title inventory
   - `KtInventorySequence`: sequence-backed paginated string-title inventory without `lastPage`
   - `KtInventorySequenceAdventure`: sequence-backed paginated Adventure-title inventory without `lastPage`
   - `KtInventoryFetched`: condition-based fetched string-title inventory for offset, cursor, or filter pagination
   - `KtInventoryFetchedAdventure`: condition-based fetched Adventure-title inventory
   - `KtInventoryLazyFetched`: fetched inventory that opens immediately and loads data asynchronously
   - `KtInventoryLazyFetchedAdventure`: lazy fetched Adventure-title inventory
   Recommendation for multi-page inventories:
   - Start with `KtInventoryPaginated` / `KtInventoryPaginatedAdventure` for ordinary static lists or lists that can be built up front.
   - Use `KtInventorySequence` / `KtInventorySequenceAdventure` only when entries should be lazy and `lastPage` is not needed.
   - Use `KtInventoryFetched` / `KtInventoryFetchedAdventure` when an offset, cursor, filter, or search condition controls page loading.
   - Use `KtInventoryLazyFetched` / `KtInventoryLazyFetchedAdventure` when opening should be immediate and slow IO should run asynchronously.
2. Prefer a primary constructor that accepts `KtInventoryPluginContext`. Callers should pass an injected context directly, or pass a `Plugin` instance when that is what they have; convert `Plugin` to `KtInventoryPluginContext` inside the inventory definition.
   - Use `KtInventoryPluginContext.LazyFetchable` only for lazy fetched inventories.
3. Define buttons in `init` with `button(...)` or `createButton(...)`. In click handlers, prefer `event.player` or a safe cast from `event.whoClicked`.
4. For paginated UIs, define `entries`, call `paginateSlot(...)`, and wire navigation with `previousPageButton(...)` and `nextPageButton(...)`. Use `KtInventorySequence` or `KtInventorySequenceAdventure` when `entries` should be a `Sequence`, `KtInventoryFetched` when page data should be loaded by condition, and `KtInventoryLazyFetched` when data should be loaded asynchronously after opening.
5. For stateful refreshes, follow the repository pattern: `companion object : Refreshable<...>(...)` and rebuild a fresh inventory in `createNew(...)`.
6. For editable storage areas, use `storable(...)` and keep save behavior inside the provided callback instead of scattering inventory persistence logic. In multi-page inventories, define the storable on the parent inventory; callbacks receive the page entry as the receiver, so use `page` or `condition` directly inside `initialize` and `save`.
7. For top inventory or viewer lookup, use the current APIs:
   - `getTopInventory<T>(player)` for the top holder a player has open.
   - `getTopInventoryPaginated<T>(player)` for the paginated inventory itself.
   - `getTopInventoryPaginatedEntry<T>(player)` when page state is needed.
   - `getTopInventorySequenceEntry<T>(player)` for sequence-backed page state.
   - `getViewers<T>()`, `getViewersPaginated<T>()`, `getViewersPaginatedEntry<T>()`, `getViewersSequenceEntry<T>()`, or `getViewersDeeply<T>()` for viewer maps.
8. Avoid deprecated lookup APIs in new code: `getOpenInventory`, `getOpenInventoryPaginated`, `getAllViewers`, `getAllViewersPaginated`, and `getAllViewersDeeply`.
9. Start from the smallest working pattern, then add pagination, refresh, or storage only when the user actually needs them.

## Repository Guidance

- Prefer small inventories with explicit slot numbers first. Expand to shared buttons, pagination, or refresh only after the basic click flow is correct.
- For multi-page inventories, prefer the least specialized class that matches the data source: prebuilt collection -> `KtInventoryPaginated`, lazy generated source -> `KtInventorySequence`, condition/cursor source -> `KtInventoryFetched`, slow IO after opening -> `KtInventoryLazyFetched`.
- Do not make callers wrap a plugin with `KtInventoryPluginContext(plugin)`. If a plugin instance is available, expose a plugin-taking bridge constructor on the inventory and convert there.
- Custom `KtInventoryPluginContext` implementations must use `KtInventoryHandlerId.of(plugin)` for `handlerId` with the same plugin used to register events.
- Fixed buttons, pagination slots, and storable slots are exclusive. Do not assign the same slot to more than one role.
- Deprecated APIs and their removal schedule are documented in `DEPRECATION.md`; preserve old APIs only when maintaining compatibility.
- Keep examples and docs aligned with the current snapshot version from the root Gradle build.
- Prefer `./gradlew build` for verification. Use a Paper server launch task only when the task depends on live in-game behavior.

## References

- Read `references/patterns.md` for copyable usage patterns.
