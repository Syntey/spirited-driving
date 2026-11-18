package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Removes the past drive with the ID from the backend, then from the device, and clears its drive feedback question and
 * notification. Throws `OfflineException` without a connection, leaving the drive as it was.
 */
@Factory
internal class DeletePastDriveUseCase(
    private val driveRecorder: DriveRecorder,
    private val tripHistoryCache: TripHistoryCache,
    private val openDriveQuestion: OpenDriveQuestion,
    @Provided private val driveReminders: DriveReminders,
) : UseCase<String, Unit> {

    override suspend fun build(args: String) {
        driveRecorder.remove(args)
        tripHistoryCache.remove(args)
        openDriveQuestion.clear(args)
        driveReminders.cancel(args)
    }
}
