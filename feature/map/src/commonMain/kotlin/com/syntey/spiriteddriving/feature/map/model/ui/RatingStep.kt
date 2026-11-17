package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import dev.icerock.moko.resources.desc.StringDesc

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
)

/** A reason chip of the rating step. */
data class ReasonChip(val reason: RatingReason, val label: StringDesc, val isSelected: Boolean)
