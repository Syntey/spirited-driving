package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * [duration] as the sheet shows it, rounded to whole minutes, at least one: "38 min", "1 h 5 min" or "2 h". The app's English
 * text, so the hours are short.
 */
internal fun durationText(duration: Duration): StringDesc {
    val minutes = max(1, duration.toDouble(DurationUnit.MINUTES).roundToInt())
    val hours = minutes / MINUTES_PER_HOUR
    val remainingMinutes = minutes % MINUTES_PER_HOUR
    return when {
        hours == 0 -> StringDesc.ResourceFormatted(MR.strings.unit_minutes, remainingMinutes)
        remainingMinutes == 0 -> StringDesc.ResourceFormatted(MR.strings.unit_hours, hours)
        else -> StringDesc.ResourceFormatted(MR.strings.unit_hours_minutes, hours, remainingMinutes)
    }
}

private const val MINUTES_PER_HOUR = 60
