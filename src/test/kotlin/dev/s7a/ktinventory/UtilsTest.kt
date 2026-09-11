package dev.s7a.ktinventory

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun `slot calculates single and ranged positions`() {
        assertEquals(13, slot(1, 4))
        assertEquals(listOf(9, 10, 11), slot(1, 0..2))
        assertEquals(listOf(2, 11, 20), slot(0..2, 2))
        assertEquals(listOf(0, 1, 9, 10), slot(0..1, 0..1))
    }
}
