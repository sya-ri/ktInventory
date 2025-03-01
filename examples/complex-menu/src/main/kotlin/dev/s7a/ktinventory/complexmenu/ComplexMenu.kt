package dev.s7a.ktinventory.complexmenu

import dev.s7a.ktinventory.KtInventory
import dev.s7a.ktinventory.complexmenu.utils.itemStack
import dev.s7a.ktinventory.slot
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.Plugin

class ComplexMenu(
    plugin: Plugin,
) : KtInventory(plugin, 6) {
    override fun title() = "&0&lMenu"

    private val border =
        button(
            slot(0..3, 4) + slot(4, 0..8),
            itemStack(Material.GRAY_STAINED_GLASS_PANE, "&0"),
        )

    private val leftStore =
        storable(
            slot(0..3, x = 0..3),
        ) {
            Bukkit.broadcastMessage("Left: ${it.filterNotNull().joinToString { "${it.type.name} x${it.amount}" }}")
        }

    private val rightStore =
        storable(
            slot(0..3, x = 5..8),
        ) {
            Bukkit.broadcastMessage("Right: ${it.filterNotNull().joinToString { "${it.type.name} x${it.amount}" }}")
        }

    private val leftSave =
        button(
            slot(5, 0),
            itemStack(Material.EMERALD, "&aSave"),
        ) {
            leftStore.save()
        }

    private val leftClear =
        button(
            slot(5, 1),
            itemStack(Material.LAVA_BUCKET, "&cClear"),
        ) {
            leftStore.clear()
        }

    private val swap =
        button(slot(5, 4), itemStack(Material.ITEM_FRAME, "&6Swap")) {
            val leftItems = leftStore.get()
            val rightItems = rightStore.get()
            leftStore.update(rightItems)
            rightStore.update(leftItems)
        }

    private val rightClear =
        button(
            slot(5, 7),
            itemStack(Material.LAVA_BUCKET, "&cClear"),
        ) {
            rightStore.clear()
        }

    private val rightSave =
        button(
            slot(5, 8),
            itemStack(Material.EMERALD, "&aSave"),
        ) {
            rightStore.save()
        }
}
