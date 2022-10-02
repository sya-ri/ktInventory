package dev.s7a.ktinventory.internal

import org.bukkit.ChatColor

internal fun String.color(altColorChar: Char): String = ChatColor.translateAlternateColorCodes(altColorChar, this)

internal fun String.color(altColorChar: Char?): String = altColorChar?.let(::color) ?: this
