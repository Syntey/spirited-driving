package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.datetime.driveDateSpokenText
import com.syntey.spiriteddriving.core.resources.datetime.driveDateText
import com.syntey.spiriteddriving.core.resources.measure.distanceText
import com.syntey.spiriteddriving.feature.routes.tools.Constants
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/**
 * A past drive as its list row shows it.
 *
 * @property destination The destination's name, or its area when it has no name.
 * @property details "Thu 5 Nov · From Smíchov · 58 km", with "Fastest" for the fastest route.
 * @property description The row as a screen reader reads it, in one sentence.
 */
data class PastDriveRow(
    val id: String,
    val destination: String,
    val details: StringDesc,
    val end: PastDriveRowEnd,
    val description: StringDesc,
)

/** What a row shows at its end. The thumb of a rating isn't a button: a rating can't be changed from the list. */
sealed interface PastDriveRowEnd {
    data class Rated(val thumb: Thumb) : PastDriveRowEnd

    /** A fun drive from the last [Constants.TripHistory.RATING_WINDOW], not rated yet. */
    data object Rate : PastDriveRowEnd

    data object None : PastDriveRowEnd
}

/** This drive as a row, with its distance in [distanceUnit] and its day as seen at [now] in [timeZone]. */
fun PastDrive.toRow(now: Instant, timeZone: TimeZone, distanceUnit: DistanceUnit): PastDriveRow {
    val area = start.shortArea
    val distanceText = distanceText(distance, distanceUnit, isApproximate = false)
    val destinationName = destination.name ?: destination.area.orEmpty()
    val place = spokenPlace(destinationName, driveDateSpokenText(startedAt, now, timeZone), area, distanceText)
    val thumb = rating?.thumb
    return PastDriveRow(
        id = id,
        destination = destinationName,
        details = details(driveDateText(startedAt, now, timeZone), area, kind == RouteKind.Fastest, distanceText),
        end = end(now, thumb),
        description = if (thumb == null) place else ratedDescription(place, thumb),
    )
}

private fun PastDrive.end(now: Instant, thumb: Thumb?): PastDriveRowEnd = when {
    thumb != null -> PastDriveRowEnd.Rated(thumb)
    kind == RouteKind.Fun && now - startedAt < Constants.TripHistory.RATING_WINDOW -> PastDriveRowEnd.Rate
    else -> PastDriveRowEnd.None
}

private fun details(day: StringDesc, area: String?, fastest: Boolean, distance: StringDesc): StringDesc = when {
    area != null && fastest -> StringDesc.ResourceFormatted(MR.strings.trip_history_row_fastest, day, area, distance)
    area != null -> StringDesc.ResourceFormatted(MR.strings.trip_history_row, day, area, distance)
    fastest -> StringDesc.ResourceFormatted(MR.strings.trip_history_row_fastest_no_start, day, distance)
    else -> StringDesc.ResourceFormatted(MR.strings.trip_history_row_no_start, day, distance)
}

private fun spokenPlace(destination: String, day: StringDesc, area: String?, distance: StringDesc): StringDesc = if (area != null) {
    StringDesc.ResourceFormatted(MR.strings.trip_history_description, destination, day, area, distance)
} else {
    StringDesc.ResourceFormatted(MR.strings.trip_history_description_no_start, destination, day, distance)
}

private fun ratedDescription(place: StringDesc, thumb: Thumb): StringDesc {
    val rated = when (thumb) {
        Thumb.Up -> MR.strings.trip_history_rated_good
        Thumb.Down -> MR.strings.trip_history_rated_not_good
    }
    return StringDesc.ResourceFormatted(MR.strings.trip_history_description_rated, place, rated.desc())
}
