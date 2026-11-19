package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.datetime.monthHeaderText
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.time.Instant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** The drives of one month, under its [header]. */
data class TripHistorySection(val header: StringDesc, val rows: ImmutableList<PastDriveRow>)

/**
 * The drives, newest first, as one section per month: consecutive drives that started in the same month of [timeZone]
 * share a section.
 */
fun List<PastDrive>.toSections(now: Instant, timeZone: TimeZone, distanceUnit: DistanceUnit): ImmutableList<TripHistorySection> {
    val months = mutableListOf<MutableList<PastDrive>>()
    var lastMonth: Pair<Int, Month>? = null
    for (drive in this) {
        val started = drive.startedAt.toLocalDateTime(timeZone)
        val month = started.year to started.month
        if (month != lastMonth) {
            months.add(mutableListOf())
            lastMonth = month
        }
        months.last().add(drive)
    }
    return months.map { drives ->
        TripHistorySection(
            header = monthHeaderText(drives.first().startedAt, now, timeZone),
            rows = drives.map { it.toRow(now, timeZone, distanceUnit) }.toImmutableList(),
        )
    }.toImmutableList()
}
