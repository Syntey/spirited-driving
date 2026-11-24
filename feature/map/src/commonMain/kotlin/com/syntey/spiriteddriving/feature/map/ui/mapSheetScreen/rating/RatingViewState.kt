package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating

import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.measure.distanceText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import com.syntey.spiriteddriving.core.resources.rating.label
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.ReasonChip
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * What the rating step shows.
 *
 * @property step The rating step: the drive's question, the thumb and reasons picked so far, and the reminder offer.
 * @property distanceUnit The unit the drive's distance is shown in.
 */
data class RatingViewState(val step: RatingStep, val distanceUnit: DistanceUnit = DistanceUnit.Kilometres) {
    /** The rating step's title: the question about the destination. */
    val title: StringDesc
        get() {
            val name = step.question.destination.name ?: return MR.strings.drive_feedback_question_unnamed.desc()
            return StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, name)
        }

    /** The drive's time, planned duration and distance under the title. */
    val driveText: StringDesc
        get() = StringDesc.ResourceFormatted(
            MR.strings.drive_feedback_drive,
            step.driveTime,
            durationText(step.question.duration),
            distanceText(step.question.distance, distanceUnit, isApproximate = false),
        )

    /** The thanks for the picked thumb; null before a thumb is picked. */
    val thanks: StringDesc?
        get() = when (step.rating?.thumb) {
            null -> null
            Thumb.Up -> MR.strings.drive_feedback_thanks_good.desc()
            Thumb.Down -> MR.strings.drive_feedback_thanks_not_good.desc()
        }

    /** The reason chips of the picked thumb, in order; empty before a thumb is picked. */
    val reasonChips: ImmutableList<ReasonChip>
        get() {
            val rating = step.rating ?: return persistentListOf()
            return RatingReason.of(rating.thumb).map { reason ->
                ReasonChip(reason = reason, label = reason.label(), isSelected = reason in rating.reasons)
            }.toImmutableList()
        }

    /** The offer of the drive reminder above Done in the rating step. */
    val showsReminderOffer: Boolean get() = step.showsReminderOffer
}
