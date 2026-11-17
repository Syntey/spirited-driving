package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import dev.icerock.moko.resources.desc.StringDesc

/**
 * The reminder about a drive the user hasn't rated yet, shown over the map above the sheet.
 *
 * @property day When the drive was, as the bubble shows it: "Yesterday".
 */
data class ReminderBubble(val question: DriveQuestion, val day: StringDesc)
