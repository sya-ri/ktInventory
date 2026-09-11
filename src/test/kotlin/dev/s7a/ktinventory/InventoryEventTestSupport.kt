package dev.s7a.ktinventory

import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation

internal fun PlayerMock.clickInventorySlot(
    rawSlot: Int,
    clickType: ClickType = ClickType.LEFT,
): InventoryClickEvent = PlayerSimulation(this).simulateInventoryClick(openInventory, clickType, rawSlot)
