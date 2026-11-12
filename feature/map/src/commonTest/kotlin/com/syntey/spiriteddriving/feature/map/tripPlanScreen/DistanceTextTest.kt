package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceTextTest {

    @Test
    fun `830 metres rounds to the nearest 50 metres`() {
        assertEquals("850 m", distanceText(830.0.metres, DistanceUnit.Kilometres, isApproximate = false))
    }

    @Test
    fun `a few metres still show at least 50 metres`() {
        assertEquals("50 m", distanceText(10.0.metres, DistanceUnit.Kilometres, isApproximate = false))
    }

    @Test
    fun `990 metres rounds up to one kilometre with a decimal`() {
        assertEquals("1.0 km", distanceText(990.0.metres, DistanceUnit.Kilometres, isApproximate = false))
    }

    @Test
    fun `4240 metres shows one decimal in kilometres`() {
        assertEquals("4.2 km", distanceText(4240.0.metres, DistanceUnit.Kilometres, isApproximate = false))
    }

    @Test
    fun `9960 metres rounds up to whole ten kilometres`() {
        assertEquals("10 km", distanceText(9960.0.metres, DistanceUnit.Kilometres, isApproximate = false))
    }

    @Test
    fun `27600 metres shows whole kilometres`() {
        assertEquals("28 km", distanceText(27600.0.metres, DistanceUnit.Kilometres, isApproximate = false))
    }

    @Test
    fun `500 metres in miles shows one decimal`() {
        assertEquals("0.3 mi", distanceText(500.0.metres, DistanceUnit.Miles, isApproximate = false))
    }

    @Test
    fun `6760 metres in miles shows one decimal`() {
        assertEquals("4.2 mi", distanceText(6760.0.metres, DistanceUnit.Miles, isApproximate = false))
    }

    @Test
    fun `27600 metres in miles shows whole miles`() {
        assertEquals("17 mi", distanceText(27600.0.metres, DistanceUnit.Miles, isApproximate = false))
    }

    @Test
    fun `approximate kilometres show whole kilometres`() {
        assertEquals("28 km", distanceText(27600.0.metres, DistanceUnit.Kilometres, isApproximate = true))
    }

    @Test
    fun `approximate distance under a kilometre shows at least one kilometre`() {
        assertEquals("1 km", distanceText(300.0.metres, DistanceUnit.Kilometres, isApproximate = true))
    }

    @Test
    fun `approximate miles show whole miles`() {
        assertEquals("4 mi", distanceText(6760.0.metres, DistanceUnit.Miles, isApproximate = true))
    }
}
