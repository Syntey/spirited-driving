package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.tools.durationText
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationTextTest {

    @Test
    fun `under an hour shows minutes`() {
        assertEquals(unitMinutes(38), durationText(38.minutes))
    }

    @Test
    fun `an hour and some minutes shows both`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.unit_hours_minutes, 1, 5),
            durationText(65.minutes),
        )
    }

    @Test
    fun `a whole number of hours drops the minutes`() {
        assertEquals(StringDesc.ResourceFormatted(MR.strings.unit_hours, 2), durationText(2.hours))
    }

    @Test
    fun `no time shows one minute`() {
        assertEquals(unitMinutes(1), durationText(0.minutes))
    }

    @Test
    fun `seconds round to the nearest minute`() {
        assertEquals(unitMinutes(2), durationText(90.seconds))
    }

    @Test
    fun `minutes that round up to an hour show the hour`() {
        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.unit_hours, 1),
            durationText(59.6.minutes),
        )
    }

    private fun unitMinutes(count: Int) = StringDesc.ResourceFormatted(MR.strings.unit_minutes, count)
}
