package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.datetime.driveTimeText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.TimeZone

/**
 * The rating sheet over the list: the question about a past drive, and its rating so far. It's the rating step of Drive
 * feedback without the route's badge, the attribution and the reminder offer.
 *
 * @property title "How was the route to Křivoklát Castle?"
 * @property subtitle The drive's time, duration and distance: "Yesterday, 16:40 · 1 h 20 · 74 km".
 * @property rating What the user picked so far; null before a thumb.
 */
data class RatingSheet(val driveId: String, val title: StringDesc, val subtitle: StringDesc, val rating: Rating? = null) {

    /** The thanks for the picked thumb; null before a thumb is picked. */
    val thanks: StringDesc?
        get() = when (rating?.thumb) {
            null -> null
            Thumb.Up -> MR.strings.drive_feedback_thanks_good.desc()
            Thumb.Down -> MR.strings.drive_feedback_thanks_not_good.desc()
        }

    /** The reason chips of the picked thumb, in order; empty before a thumb is picked. */
    val reasonChips: ImmutableList<ReasonChip>
        get() {
            val rating = rating ?: return persistentListOf()
            return RatingReason.of(rating.thumb).map { reason ->
                ReasonChip(reason = reason, label = reason.label(), isSelected = reason in rating.reasons)
            }.toImmutableList()
        }
}

/** A reason chip of the rating sheet. */
data class ReasonChip(val reason: RatingReason, val label: StringDesc, val isSelected: Boolean)

/** The rating sheet for this drive, as it's rated now, with its day and time seen at [now] in [timeZone]. */
fun PastDrive.toRatingSheet(now: Instant, timeZone: TimeZone, distanceUnit: DistanceUnit): RatingSheet {
    val name = destination.name
    val title = if (name == null) {
        MR.strings.drive_feedback_question_unnamed.desc()
    } else {
        StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, name)
    }
    return RatingSheet(
        driveId = id,
        title = title,
        subtitle = StringDesc.ResourceFormatted(
            MR.strings.drive_feedback_drive,
            driveTimeText(startedAt, now, timeZone),
            durationText(duration),
            distanceText(distance, distanceUnit, isApproximate = false),
        ),
        rating = rating,
    )
}
