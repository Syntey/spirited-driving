package com.syntey.spiriteddriving.data.persistence.persistence.drive

import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import kotlinx.serialization.Serializable

/** The kept trip history as stored: its drives, newest first, and the cursor of the page after them. */
@Serializable
internal data class StoredTripHistory(val drives: List<StoredPastDrive>, val nextCursor: String?)

internal fun PastDrivePage.toStored() = StoredTripHistory(drives = drives.map { it.toStored() }, nextCursor = nextCursor)

internal fun StoredTripHistory.toDomain() = PastDrivePage(drives = drives.map { it.toDomain() }, nextCursor = nextCursor)
