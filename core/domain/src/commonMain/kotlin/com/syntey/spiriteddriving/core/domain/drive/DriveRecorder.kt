package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Records drives on the backend, and removes them. Recording a drive repeatedly is safe. Recording throws [OfflineException]
 * without a connection and [DriveNotRecordableException] when the backend won't ever take the drive: its route's plan
 * expired, or its ID belongs to another route.
 */
interface DriveRecorder {

    suspend fun record(drive: Drive)

    /** Removes the drive and its rating from the backend. Removing a drive that's gone, or isn't this user's, is done too. */
    suspend fun remove(id: String)
}
