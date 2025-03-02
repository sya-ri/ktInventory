package dev.s7a.ktinventory.example

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun itemStack(
    material: Material,
    displayName: String,
) = ItemStack(material).apply {
    itemMeta =
        itemMeta?.apply {
            setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName))
        }
}
