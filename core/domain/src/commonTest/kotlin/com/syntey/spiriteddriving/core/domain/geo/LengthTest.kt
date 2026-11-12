package com.syntey.spiriteddriving.core.domain.geo

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LengthTest {

    @Test
    fun `1 kilometre is 1000 metres`() {
        assertEquals(1000.0, 1.kilometres.inMetres)
    }

    @Test
    fun `1 mile is 1609 point 344 metres`() {
        assertEquals(1609.344, 1.miles.inMetres, absoluteTolerance = 1e-9)
    }

    @Test
    fun `whole metres round to the nearest metre`() {
        assertEquals(1609, 1.miles.inWholeMetres)
    }

    @Test
    fun `whole metres round a kilometre fraction to the nearest metre`() {
        assertEquals(1600, 1.6.kilometres.inWholeMetres)
    }

    @Test
    fun `half a mile rounds to 805 whole metres`() {
        assertEquals(805, 0.5.miles.inWholeMetres)
    }

    @Test
    fun `1500 metres is 1 point 5 kilometres`() {
        assertEquals(1.5, 1500.metres.inKilometres)
    }

    @Test
    fun `1 mile is 1 mile in miles`() {
        assertEquals(1.0, 1.miles.inMiles, absoluteTolerance = 1e-9)
    }

    @Test
    fun `1 kilometre is about 0 point 621 miles`() {
        assertEquals(0.621371, 1.kilometres.inMiles, absoluteTolerance = 1e-6)
    }

    @Test
    fun `lengths add`() {
        assertEquals(1500.metres, 1.kilometres + 500.metres)
    }

    @Test
    fun `lengths subtract`() {
        assertEquals(500.metres, 1.kilometres - 500.metres)
    }

    @Test
    fun `1 mile is longer than 1 kilometre`() {
        assertTrue(1.miles > 1.kilometres)
    }

    @Test
    fun `zero is no length`() {
        assertEquals(0.0, Length.Zero.inMetres)
    }
}
