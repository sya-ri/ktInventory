package dev.s7a.ktinventory.soundchecker

import dev.s7a.ktinventory.KtInventory
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

private fun String.color(altColorChar: Char): String = ChatColor.translateAlternateColorCodes(altColorChar, this)

private fun String.color(altColorChar: Char?): String = altColorChar?.let(::color) ?: this

fun KtInventory.item(
    index: Int,
    type: Material,
    displayName: String,
    altColorChar: Char? = '&',
    block: (InventoryClickEvent) -> Unit,
) {
    item(
        index,
        ItemStack(type).apply {
            itemMeta =
                itemMeta?.apply {
                    setDisplayName(displayName.color(altColorChar))
                }
        },
        block,
    )
}
