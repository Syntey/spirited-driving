package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Sends drive ratings to the backend. Sending the same rating again is safe; a new one replaces it. Throws
 * [OfflineException] without a connection and [DriveNotRatableException] when the backend won't ever take it: it doesn't
 * have the drive, or the drive is another user's.
 */
interface DriveRater {

    suspend fun rate(driveId: String, rating: Rating)
}
