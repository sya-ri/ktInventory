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
   - `KtInventoryPaginatedSequence`: sequence-backed paginated string-title inventory without `lastPage`
   - `KtInventoryPaginatedSequenceAdventure`: sequence-backed paginated Adventure-title inventory without `lastPage`
   - `KtInventoryPaginatedFetched`: condition-based fetched string-title inventory for offset, cursor, or filter pagination
   - `KtInventoryPaginatedFetchedAdventure`: condition-based fetched Adventure-title inventory
   - `KtInventoryPaginatedLazyFetched`: fetched inventory that opens immediately and loads data asynchronously
   - `KtInventoryPaginatedLazyFetchedAdventure`: lazy fetched Adventure-title inventory
   Recommendation for multi-page inventories:
   - Start with `KtInventoryPaginated` / `KtInventoryPaginatedAdventure` for ordinary static lists or lists that can be built up front.
   - Use `KtInventoryPaginatedSequence` / `KtInventoryPaginatedSequenceAdventure` only when entries should be lazy and `lastPage` is not needed.
   - Use `KtInventoryPaginatedFetched` / `KtInventoryPaginatedFetchedAdventure` when an offset, cursor, filter, or search condition controls page loading.
   - Use `KtInventoryPaginatedLazyFetched` / `KtInventoryPaginatedLazyFetchedAdventure` when opening should be immediate and slow IO should run asynchronously.
2. Prefer a primary constructor that accepts `KtInventoryPluginContext`. Callers should pass an injected context directly, or pass a `Plugin` instance when that is what they have; convert `Plugin` to `KtInventoryPluginContext` inside the inventory definition.
   - Use `KtInventoryPluginContext.LazyFetchable` only for lazy fetched inventories.
3. Define buttons in `init` with `button(...)` or `createButton(...)`. In click handlers, prefer `event.player` or a safe cast from `event.whoClicked`.
4. For paginated UIs, define `entries`, call `paginateSlot(...)`, and wire navigation with `previousPageButton(...)` and `nextPageButton(...)`. Use `KtInventoryPaginatedSequence` or `KtInventoryPaginatedSequenceAdventure` when `entries` should be a `Sequence`, `KtInventoryPaginatedFetched` when page data should be loaded by condition, and `KtInventoryPaginatedLazyFetched` when data should be loaded asynchronously after opening.
5. For stateful refreshes, follow the repository pattern: `companion object : Refreshable<...>(...)` and rebuild a fresh inventory in `createNew(...)`. Paginated fetched and lazy fetched refreshables use `RefreshBehavior.Keep` to preserve the current condition and `RefreshBehavior.OpenFirst` to reopen from `initialCondition`.
6. For editable storage areas, use `storable(...)` and keep save behavior inside the provided callback instead of scattering inventory persistence logic. In multi-page inventories, define the storable on the parent inventory; callbacks receive the page entry as the receiver, so use `page` or `condition` directly inside `initialize` and `save`.
7. For top inventory or viewer lookup, use the current APIs:
   - `getTopInventory<T>(player)` for the top holder a player has open, including the paginated inventory behind paginated entries.
   - `getTopInventoryPaginatedEntry<T>(player)` when page state is needed.
   - `getTopInventoryPaginatedSequenceEntry<T>(player)` for sequence-backed page state.
   - `getTopInventoryPaginatedFetchedEntry<T>(player)` for condition-fetched page state.
   - `getTopInventoryPaginatedLazyFetchedEntry<T>(player)` for lazy-fetched page/load state.
   - `getViewers<T>()` for viewer maps of top inventory holders.
   - `getViewersPaginatedEntry<T>()` for viewer maps of paginated entries.
   - `getViewersPaginatedSequenceEntry<T>()` for viewer maps of sequence-backed paginated entries.
   - `getViewersPaginatedFetchedEntry<T>()` for viewer maps of condition-fetched paginated entries.
   - `getViewersPaginatedLazyFetchedEntry<T>()` for viewer maps of lazy-fetched paginated entries.
   - `getViewersDeeply<T>()` for viewer maps resolved through parent inventory relationships.
8. Avoid deprecated lookup APIs in new code: `getOpenInventory`, `getOpenInventoryPaginated`, `getAllViewers`, `getAllViewersPaginated`, and `getAllViewersDeeply`.
9. Start from the smallest working pattern, then add pagination, refresh, or storage only when the user actually needs them.

## Repository Guidance

- Prefer small inventories with explicit slot numbers first. Expand to shared buttons, pagination, or refresh only after the basic click flow is correct.
- For multi-page inventories, prefer the least specialized class that matches the data source: prebuilt collection -> `KtInventoryPaginated`, lazy generated source -> `KtInventoryPaginatedSequence`, condition/cursor source -> `KtInventoryPaginatedFetched`, slow IO after opening -> `KtInventoryPaginatedLazyFetched`.
- Do not make callers wrap a plugin with `KtInventoryPluginContext(plugin)`. If a plugin instance is available, expose a plugin-taking bridge constructor on the inventory and convert there.
- Custom `KtInventoryPluginContext` implementations must use `KtInventoryHandlerId.of(plugin)` for `handlerId` with the same plugin used to register events.
- Fixed buttons, pagination slots, and storable slots are exclusive. Do not assign the same slot to more than one role.
- Deprecated APIs and their removal schedule are documented in `DEPRECATION.md`; preserve old APIs only when maintaining compatibility.
- Keep examples and docs aligned with the current snapshot version from the root Gradle build.
- Prefer `./gradlew build` for verification. Use a Paper server launch task only when the task depends on live in-game behavior.

## References

- Read `references/patterns.md` for copyable usage patterns.
