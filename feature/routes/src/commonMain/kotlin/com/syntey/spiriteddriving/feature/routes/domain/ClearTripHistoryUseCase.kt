package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.TripHistorySource
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Removes all of the user's past drives and their ratings from the backend, then from the device, and clears the drive
 * feedback question and notification when one is open. Throws `OfflineException` without a connection, leaving the history as it was.
 */
@Factory
internal class ClearTripHistoryUseCase(
    private val tripHistorySource: TripHistorySource,
    private val tripHistoryCache: TripHistoryCache,
    private val openDriveQuestion: OpenDriveQuestion,
    @Provided private val driveReminders: DriveReminders,
) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) {
        tripHistorySource.clear()
        // An empty page rather than none, so the list shows its empty state instead of loading
        tripHistoryCache.replaceAll(PastDrivePage(drives = emptyList(), nextCursor = null))
        val question = openDriveQuestion.get() ?: return
        openDriveQuestion.clear(question.driveId)
        driveReminders.cancel(question.driveId)
    }
}
