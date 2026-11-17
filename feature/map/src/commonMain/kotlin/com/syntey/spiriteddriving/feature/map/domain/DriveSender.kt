package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRater
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import org.koin.core.annotation.Factory

/**
 * Brings the backend up to date with one kept drive: records it, removes it once the user said they didn't drive it, and
 * sends its rating. Throws `OfflineException` without a connection, leaving the rest for a later try.
 */
@Factory
internal class DriveSender(
    private val startedDrives: StartedDrives,
    private val driveRecorder: DriveRecorder,
    private val driveRater: DriveRater,
) {

    suspend fun send(drive: Drive) {
        if (drive.isNotDriven) {
            removeDrive(drive)
        } else if (drive.isRecorded || record(drive)) {
            sendRating(drive)
        }
    }

    // A drive the backend never had is only removed here, as there's nothing to remove there
    private suspend fun removeDrive(drive: Drive) {
        if (drive.isRecorded) driveRecorder.remove(drive.id)
        startedDrives.remove(drive.id)
    }

    // False when the backend won't take the drive ever, which removes it here as there's nothing to keep it for
    @Suppress("SwallowedException")
    private suspend fun record(drive: Drive): Boolean {
        try {
            driveRecorder.record(drive)
        } catch (error: DriveNotRecordableException) {
            startedDrives.remove(drive.id)
            return false
        }
        startedDrives.markRecorded(drive.id)
        return true
    }

    @Suppress("SwallowedException")
    private suspend fun sendRating(drive: Drive) {
        val rating = drive.rating ?: return
        if (drive.isRatingSent) return
        try {
            driveRater.rate(drive.id, rating)
        } catch (error: DriveNotRatableException) {
            // The backend won't take this rating ever, so retrying can't succeed; it's marked sent to stop the retries
        }
        startedDrives.markRatingSent(drive.id, rating)
    }
}
