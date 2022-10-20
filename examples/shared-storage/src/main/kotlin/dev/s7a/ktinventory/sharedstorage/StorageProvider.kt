package dev.s7a.ktinventory.sharedstorage

import org.bukkit.inventory.ItemStack

interface StorageProvider {
    var contents: List<ItemStack?>
}
