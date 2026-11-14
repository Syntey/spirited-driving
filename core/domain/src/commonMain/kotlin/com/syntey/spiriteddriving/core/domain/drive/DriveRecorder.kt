package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Records a drive on the backend. Repeating it for the same drive is safe. Throws [OfflineException] without a connection
 * and [DriveNotRecordableException] when the backend won't ever take the drive: its route's plan expired, or its ID
 * belongs to another route.
 */
interface DriveRecorder {

    suspend fun record(drive: Drive)
}
