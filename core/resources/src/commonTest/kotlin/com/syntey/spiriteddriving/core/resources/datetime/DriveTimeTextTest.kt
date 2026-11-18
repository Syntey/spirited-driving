package com.syntey.spiriteddriving.core.resources.datetime

import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class DriveTimeTextTest {

    private val prague = TimeZone.of("Europe/Prague")

    @Test
    fun `a drive from today shows today and its time`() {
        val text = driveTimeText(startedAt = instant("2026-11-16T13:20:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals(StringDesc.ResourceFormatted(MR.strings.drive_feedback_today, "14:20"), text)
    }

    @Test
    fun `a drive from before midnight shows yesterday the day after`() {
        val text = driveTimeText(startedAt = instant("2026-11-15T22:50:00Z"), now = instant("2026-11-15T23:10:00Z"), timeZone = prague)

        assertEquals(StringDesc.ResourceFormatted(MR.strings.drive_feedback_yesterday, "23:50"), text)
    }

    @Test
    fun `a drive from two days back shows its weekday`() {
        val text = driveTimeText(startedAt = instant("2026-11-14T13:20:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals(
            StringDesc.ResourceFormatted(MR.strings.drive_feedback_weekday, MR.strings.weekday_saturday.desc(), "14:20"),
            text,
        )
    }

    @Test
    fun `an hour before ten has no leading zero and minutes keep two digits`() {
        val text = driveTimeText(startedAt = instant("2026-11-16T08:05:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals(StringDesc.ResourceFormatted(MR.strings.drive_feedback_today, "9:05"), text)
    }

    @Test
    fun `the day of a drive from today is today`() {
        val text = driveDayText(startedAt = instant("2026-11-16T13:20:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals(MR.strings.drive_feedback_today_day.desc(), text)
    }

    @Test
    fun `the day of a drive from before midnight is yesterday`() {
        val text = driveDayText(startedAt = instant("2026-11-15T22:50:00Z"), now = instant("2026-11-15T23:10:00Z"), timeZone = prague)

        assertEquals(MR.strings.drive_feedback_yesterday_day.desc(), text)
    }

    @Test
    fun `the day of a drive from two days back is its weekday`() {
        val text = driveDayText(startedAt = instant("2026-11-14T13:20:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals(MR.strings.weekday_saturday.desc(), text)
    }

    @Test
    fun `the date of a drive from today is today`() {
        val text = driveDateText(startedAt = instant("2026-11-16T13:20:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals(MR.strings.drive_feedback_today_day.desc(), text)
    }

    @Test
    fun `the date of a drive from before midnight is yesterday`() {
        val text = driveDateText(startedAt = instant("2026-11-15T22:50:00Z"), now = instant("2026-11-16T00:10:00Z"), timeZone = prague)

        assertEquals(MR.strings.drive_feedback_yesterday_day.desc(), text)
    }

    @Test
    fun `the date of a drive from two days back is a day and month, not today or yesterday`() {
        val text = driveDateText(startedAt = instant("2026-11-14T13:20:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertNotEquals(MR.strings.drive_feedback_today_day.desc(), text)
        assertNotEquals(MR.strings.drive_feedback_yesterday_day.desc(), text)
    }

    @Test
    fun `the time zone decides which day a late drive falls on`() {
        val startedAt = instant("2026-11-15T23:30:00Z")
        val now = instant("2026-11-16T10:00:00Z")

        assertEquals(MR.strings.drive_feedback_yesterday_day.desc(), driveDateText(startedAt, now, TimeZone.UTC))
        assertEquals(MR.strings.drive_feedback_today_day.desc(), driveDateText(startedAt, now, prague))
    }

    @Test
    fun `a month in the current year has no year in its header`() {
        val pattern =
            monthHeaderPattern(startedAt = instant("2026-11-08T09:30:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals("LLLL", pattern)
    }

    @Test
    fun `a month in an earlier year has the year in its header`() {
        val pattern =
            monthHeaderPattern(startedAt = instant("2025-10-20T09:30:00Z"), now = instant("2026-11-16T15:00:00Z"), timeZone = prague)

        assertEquals("LLLL yyyy", pattern)
    }

    @Test
    fun `a drive from New Year's Eve has the year in its header once the year turns`() {
        val pattern =
            monthHeaderPattern(startedAt = instant("2025-12-31T23:30:00Z"), now = instant("2026-01-01T00:30:00Z"), timeZone = TimeZone.UTC)

        assertEquals("LLLL yyyy", pattern)
    }

    private fun instant(text: String): Instant = Instant.parse(text)
}
