package dev.s7a.ktinventory

/**
 * Calculates the slot number for a given position in the inventory.
 *
 * @param y The row number (0-based)
 * @param x The column number (0-based)
 * @return The slot number
 * @since 2.0.0
 */
fun slot(
    y: Int,
    x: Int,
) = y * 9 + x

/**
 * Calculates the slot numbers for a given row and column range.
 *
 * @param y The row number (0-based)
 * @param x The range of column numbers (0-based)
 * @return List of slot numbers
 * @since 2.0.0
 */
fun slot(
    y: Int,
    x: IntRange,
) = x.map { x -> slot(y, x) }

/**
 * Calculates the slot numbers for a given row range and column.
 *
 * @param y The range of row numbers (0-based)
 * @param x The column number (0-based)
 * @return List of slot numbers
 * @since 2.0.0
 */
fun slot(
    y: IntRange,
    x: Int,
) = y.map { y -> slot(y, x) }

/**
 * Calculates the slot numbers for given row and column ranges.
 *
 * @param y The range of row numbers (0-based)
 * @param x The range of column numbers (0-based)
 * @return List of slot numbers
 * @since 2.0.0
 */
fun slot(
    y: IntRange,
    x: IntRange,
) = y.flatMap { y -> slot(y, x) }
