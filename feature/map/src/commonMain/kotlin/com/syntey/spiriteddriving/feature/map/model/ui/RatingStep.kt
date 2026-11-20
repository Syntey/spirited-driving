package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.feature.map.tools.boundsOf
import com.syntey.spiriteddriving.feature.map.tools.midpointAlong
import com.syntey.spiriteddriving.feature.map.tools.withMinimumSpan
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.toImmutableList

/**
 * The rating step of the sheet, after a drive.
 *
 * @property driveTime When the drive was handed off, as the step shows it: "Today, 14:20".
 * @property rating What the user picked so far; null before a thumb.
 * @property showsReminderOffer Whether the offer of the drive reminder shows above Done; set after the first thumb, once.
 */
data class RatingStep(
    val question: DriveQuestion,
    val driveTime: StringDesc,
    val rating: Rating? = null,
    val showsReminderOffer: Boolean = false,
) : SheetStep {
    override val start: GeoPoint get() = question.start

    override val destination: GeoPoint get() = question.destination.location

    override val routeBounds: GeoBounds
        get() {
            // A stored line may be empty; the drive's ends frame it then
            return boundsOf(question.line.ifEmpty { listOf(question.start, question.destination.location) }).withMinimumSpan()
        }

    override val routeLine: RouteLine?
        get() {
            if (question.line.isEmpty()) return null
            return RouteLine(
                id = question.driveId,
                number = question.routeNumber,
                points = question.line.toImmutableList(),
                label = null,
                labelPoint = midpointAlong(question.line),
                isSelected = true,
            )
        }
}

/** A reason chip of the rating step. */
data class ReasonChip(val reason: RatingReason, val label: StringDesc, val isSelected: Boolean)
