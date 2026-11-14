package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory

/**
 * Records the drives that aren't recorded yet, oldest first. Stops at the first `OfflineException`, leaving the rest for the
 * next try. Removes a drive the backend won't take, and skips a drive that fails otherwise, so it's tried again next time.
 */
@Factory
internal class RecordPendingDrivesUseCase(private val startedDrives: StartedDrives, private val driveRecorder: DriveRecorder) :
    UseCase<Unit, Unit> {

    // Each failure is handled by its case: the drive is removed, the rest wait, or the drive is skipped until the next try
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun build(args: Unit) {
        val pending = startedDrives.all().filterNot { it.isRecorded }.reversed()
        for (drive in pending) {
            try {
                driveRecorder.record(drive)
                startedDrives.markRecorded(drive.id)
            } catch (error: DriveNotRecordableException) {
                startedDrives.remove(drive.id)
            } catch (error: OfflineException) {
                return
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                // Any other failure waits for the next try; the rest of the drives still go now
            }
        }
    }
}
