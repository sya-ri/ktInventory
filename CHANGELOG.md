# Changelog

## v2.1.1

### Added

- Add installable `ktinventory` skill for agent tooling.
  - Includes repository guidance for building Bukkit, Spigot, and Paper inventory UIs with `ktInventory`.
  - Documents recommended class selection, constructor usage, pagination, refresh, and storage patterns.

## v2.1.0

### Added

- Add `KtInventoryPluginContext` to simplify plugin dependency injection.
  - Replaced direct `Plugin` injection with `KtInventoryPluginContext`, which packages plugin-dependent data.
  - This simplifies constructor dependencies and makes testing or future dependency swapping easier.
  - Existing `Plugin` constructors are now `@Deprecated` and delegate to `KtInventoryPluginContext(plugin)`.
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
