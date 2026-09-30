package io.github.blackphi6.pauseresumeaudiofade

import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeFaderTest {
    @Test
    fun `fade out steps down to zero one index at a time`() {
        assertEquals(listOf(14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0), fadeSteps(15, 0))
    }

    @Test
    fun `fade in steps up to max one index at a time`() {
        assertEquals(listOf(1, 2, 3), fadeSteps(0, 3))
    }

    @Test
    fun `no movement needed when already at target`() {
        assertEquals(emptyList<Int>(), fadeSteps(5, 5))
    }
}
