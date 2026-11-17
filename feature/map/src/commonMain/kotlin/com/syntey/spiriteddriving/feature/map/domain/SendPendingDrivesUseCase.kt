package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory

/**
 * Sends the drives that still have something to send, oldest first: not recorded, not driven, or rated with the rating not
 * sent yet. Stops at the first `OfflineException`, leaving the rest for the next try. A drive that fails otherwise is
 * skipped, so it's tried again next time, and the drives after it still go now.
 */
@Factory
internal class SendPendingDrivesUseCase(private val startedDrives: StartedDrives, private val driveSender: DriveSender) :
    UseCase<Unit, Unit> {

    // Each failure is handled by its case: the rest wait, or the drive is skipped until the next try
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override suspend fun build(args: Unit) {
        val pending = startedDrives.all().filter { it.hasPendingWork() }.reversed()
        for (drive in pending) {
            try {
                driveSender.send(drive)
            } catch (error: OfflineException) {
                return
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                // Any other failure waits for the next try; the rest of the drives still go now
            }
        }
    }

    private fun Drive.hasPendingWork(): Boolean = !isRecorded || isNotDriven || (rating != null && !isRatingSent)
}
