# ktInventory

> [!WARNING]
> This library is under development, so you can't use it.

Spigot library for Kotlin for easy inventory creation and event handling

## Usage

### DSL

You can easily generate inventory. Basically, use this.

```kotlin
fun openSimpleMenu(plugin: JavaPlugin, player: HumanEntity) {
    plugin.ktInventory("&0&lSelect where to teleport", 1) {
        item(3, ItemStack(Material.RED_BED)) {
            val player = it.whoClicked as? Player ?: return@item
            val respawnLocation = player.respawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found respawnLocation")
            }
            player.closeInventory()
        }
        item(5, ItemStack(Material.COMPASS)) {
            val player = it.whoClicked as? Player ?: return@item
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }.open(player)
}
```

### Provider

Passing a plugin instance as a property makes the dependencies between classes a little complicated.
In such cases you can use Provider instead of DSL.

```kotlin
class SimpleMenu(provider: KtInventoryProvider) {
    private val inventory = provider.create("&0&lSelect where to teleport", 1) {
        item(3, ItemStack(Material.RED_BED)) {
            val player = it.whoClicked as? Player ?: return@item
            val respawnLocation = player.respawnLocation
            if (respawnLocation != null) {
                player.teleport(respawnLocation)
            } else {
                player.sendMessage("Not found respawnLocation")
            }
            player.closeInventory()
        }
        item(5, ItemStack(Material.COMPASS)) {
            val player = it.whoClicked as? Player ?: return@item
            player.teleport(player.world.spawnLocation)
            player.closeInventory()
        }
    }

    fun open(player: HumanEntity) {
        inventory.open(player)
    }
}
```
