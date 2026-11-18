package com.syntey.spiriteddriving.core.resources.measure

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceTextTest {

    @Test
    fun `830 metres rounds to the nearest 50 metres`() {
        assertEquals(
            m("850"),
            distanceText(830.0.metres, DistanceUnit.Kilometres, isApproximate = false),
        )
    }

    @Test
    fun `a few metres still show at least 50 metres`() {
        assertEquals(
            m("50"),
            distanceText(10.0.metres, DistanceUnit.Kilometres, isApproximate = false),
        )
    }

    @Test
    fun `990 metres rounds up to one kilometre with a decimal`() {
        assertEquals(
            km("1.0"),
            distanceText(990.0.metres, DistanceUnit.Kilometres, isApproximate = false),
        )
    }

    @Test
    fun `4240 metres shows one decimal in kilometres`() {
        assertEquals(
            km("4.2"),
            distanceText(4240.0.metres, DistanceUnit.Kilometres, isApproximate = false),
        )
    }

    @Test
    fun `9960 metres rounds up to whole ten kilometres`() {
        assertEquals(
            km("10"),
            distanceText(9960.0.metres, DistanceUnit.Kilometres, isApproximate = false),
        )
    }

    @Test
    fun `27600 metres shows whole kilometres`() {
        assertEquals(
            km("28"),
            distanceText(27600.0.metres, DistanceUnit.Kilometres, isApproximate = false),
        )
    }

    @Test
    fun `500 metres in miles shows one decimal`() {
        assertEquals(
            mi("0.3"),
            distanceText(500.0.metres, DistanceUnit.Miles, isApproximate = false),
        )
    }

    @Test
    fun `6760 metres in miles shows one decimal`() {
        assertEquals(
            mi("4.2"),
            distanceText(6760.0.metres, DistanceUnit.Miles, isApproximate = false),
        )
    }

    @Test
    fun `27600 metres in miles shows whole miles`() {
        assertEquals(
            mi("17"),
            distanceText(27600.0.metres, DistanceUnit.Miles, isApproximate = false),
        )
    }

    @Test
    fun `approximate kilometres show whole kilometres`() {
        assertEquals(
            km("28"),
            distanceText(27600.0.metres, DistanceUnit.Kilometres, isApproximate = true),
        )
    }

    @Test
    fun `approximate distance under a kilometre shows at least one kilometre`() {
        assertEquals(
            km("1"),
            distanceText(300.0.metres, DistanceUnit.Kilometres, isApproximate = true),
        )
    }

    @Test
    fun `approximate miles show whole miles`() {
        assertEquals(mi("4"), distanceText(6760.0.metres, DistanceUnit.Miles, isApproximate = true))
    }

    @Test
    fun `a climb in metres rounds to the nearest 10 metres`() {
        assertEquals(m("810"), heightText(811.0.metres, DistanceUnit.Kilometres))
    }

    @Test
    fun `a climb of 815 metres rounds up to 820`() {
        assertEquals(m("820"), heightText(815.0.metres, DistanceUnit.Kilometres))
    }

    @Test
    fun `a climb of over a thousand metres groups the thousands`() {
        assertEquals(m("1,250"), heightText(1250.0.metres, DistanceUnit.Kilometres))
    }

    @Test
    fun `a climb in miles is in feet rounded to 10 feet`() {
        assertEquals(ft("2,660"), heightText(811.0.metres, DistanceUnit.Miles))
    }

    @Test
    fun `no climb shows zero metres`() {
        assertEquals(m("0"), heightText(0.0.metres, DistanceUnit.Kilometres))
    }

    private fun m(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_metres, number)

    private fun km(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_kilometres, number)

    private fun mi(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_miles, number)

    private fun ft(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_feet, number)
}
