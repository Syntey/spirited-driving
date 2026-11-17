package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * When a drive started, as the rating step shows it: "Today, 14:20", "Yesterday, 9:05", or a weekday with the time further
 * back. Time is 24-hour, with two-digit minutes. The app asks only within 48 hours of the hand-off, so a weekday is never
 * ambiguous: it always falls within the last week.
 */
internal fun driveTimeText(startedAt: Instant, now: Instant, timeZone: TimeZone): StringDesc {
    val started = startedAt.toLocalDateTime(timeZone)
    val time = clockText(hour = started.hour, minute = started.minute)
    return when (val day = driveDay(started.date, now, timeZone)) {
        DriveDay.Today -> StringDesc.ResourceFormatted(MR.strings.drive_feedback_today, time)
        DriveDay.Yesterday -> StringDesc.ResourceFormatted(MR.strings.drive_feedback_yesterday, time)
        is DriveDay.Weekday -> StringDesc.ResourceFormatted(MR.strings.drive_feedback_weekday, weekdayResource(day.day).desc(), time)
    }
}

/** The day a drive started on, as the reminder bubble shows it: "Today", "Yesterday", or the weekday. */
internal fun driveDayText(startedAt: Instant, now: Instant, timeZone: TimeZone): StringDesc {
    val started = startedAt.toLocalDateTime(timeZone).date
    return when (val day = driveDay(started, now, timeZone)) {
        DriveDay.Today -> MR.strings.drive_feedback_today_day.desc()
        DriveDay.Yesterday -> MR.strings.drive_feedback_yesterday_day.desc()
        is DriveDay.Weekday -> weekdayResource(day.day).desc()
    }
}

// The day a drive started on, relative to today; shared by the time and the day text so they never disagree
private fun driveDay(started: LocalDate, now: Instant, timeZone: TimeZone): DriveDay {
    val today = now.toLocalDateTime(timeZone).date
    return when (started) {
        today -> DriveDay.Today
        today.minus(DatePeriod(days = 1)) -> DriveDay.Yesterday
        else -> DriveDay.Weekday(started.dayOfWeek)
    }
}

private sealed interface DriveDay {
    data object Today : DriveDay

    data object Yesterday : DriveDay

    data class Weekday(val day: DayOfWeek) : DriveDay
}

// "9:05" and "14:20": the hour isn't padded, the minutes are
private fun clockText(hour: Int, minute: Int): String = "$hour:${minute.toString().padStart(2, '0')}"

private fun weekdayResource(day: DayOfWeek): StringResource = when (day) {
    DayOfWeek.MONDAY -> MR.strings.weekday_monday
    DayOfWeek.TUESDAY -> MR.strings.weekday_tuesday
    DayOfWeek.WEDNESDAY -> MR.strings.weekday_wednesday
    DayOfWeek.THURSDAY -> MR.strings.weekday_thursday
    DayOfWeek.FRIDAY -> MR.strings.weekday_friday
    DayOfWeek.SATURDAY -> MR.strings.weekday_saturday
    DayOfWeek.SUNDAY -> MR.strings.weekday_sunday
}
