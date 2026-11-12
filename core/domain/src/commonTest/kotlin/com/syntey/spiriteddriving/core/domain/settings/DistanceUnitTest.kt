package com.syntey.spiriteddriving.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceUnitTest {

    @Test
    fun `the US reads distances in miles`() {
        assertEquals(DistanceUnit.Miles, DistanceUnit.of("US"))
    }

    @Test
    fun `a lowercase region code is matched`() {
        assertEquals(DistanceUnit.Miles, DistanceUnit.of("gb"))
    }

    @Test
    fun `the Czech Republic reads distances in kilometres`() {
        assertEquals(DistanceUnit.Kilometres, DistanceUnit.of("CZ"))
    }

    @Test
    fun `no region reads distances in kilometres`() {
        assertEquals(DistanceUnit.Kilometres, DistanceUnit.of(null))
    }
}
