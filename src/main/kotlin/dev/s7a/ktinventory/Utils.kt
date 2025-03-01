package dev.s7a.ktinventory

fun slot(
    y: Int,
    x: Int,
) = y * 9 + x

fun slot(
    y: Int,
    x: IntRange,
) = x.map { x -> slot(y, x) }

fun slot(
    y: IntRange,
    x: Int,
) = y.map { y -> slot(y, x) }

fun slot(
    y: IntRange,
    x: IntRange,
) = y.flatMap { y -> slot(y, x) }
