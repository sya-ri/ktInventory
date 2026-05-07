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
2. Prefer a primary constructor that accepts `KtInventoryPluginContext`. Callers should pass an injected context directly, or pass a `Plugin` instance when that is what they have; convert `Plugin` to `KtInventoryPluginContext` inside the inventory definition.
3. Define buttons in `init` with `button(...)` or `createButton(...)`. In click handlers, prefer `event.player` or a safe cast from `event.whoClicked`.
4. For paginated UIs, define `entries`, call `paginateSlot(...)`, and wire navigation with `previousPageButton(...)` and `nextPageButton(...)`.
5. For stateful refreshes, follow the repository pattern: `companion object : Refreshable<...>(...)` and rebuild a fresh inventory in `createNew(...)`.
6. For editable storage areas, use `storable(...)` and keep save behavior inside the provided callback instead of scattering inventory persistence logic.
7. Start from the smallest working pattern, then add pagination, refresh, or storage only when the user actually needs them.

## Repository Guidance

- Prefer small inventories with explicit slot numbers first. Expand to shared buttons, pagination, or refresh only after the basic click flow is correct.
- Do not make callers wrap a plugin with `KtInventoryPluginContext(plugin)`. If a plugin instance is available, expose a plugin-taking bridge constructor on the inventory and convert there.
- Keep examples and docs aligned with the current snapshot version from the root Gradle build.
- Prefer `./gradlew build` for verification. Use a Paper server launch task only when the task depends on live in-game behavior.

## References

- Read `references/patterns.md` for copyable usage patterns.
