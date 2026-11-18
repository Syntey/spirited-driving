package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRater
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Keeps [Args.rating] on a past drive and sends it. Rating a drive here ends its drive feedback question everywhere, so the
 * question is cleared and its notification cancelled. A drive started on this phone is kept up to date too, so drive feedback
 * knows it was rated. Without a connection the rating is kept and sent later, and that's not an error. Throws other failures.
 */
@Factory
internal class RatePastDriveUseCase(
    private val openDriveQuestion: OpenDriveQuestion,
    @Provided private val driveReminders: DriveReminders,
    private val tripHistoryCache: TripHistoryCache,
    private val startedDrives: StartedDrives,
    private val driveRater: DriveRater,
) : UseCase<RatePastDriveUseCase.Args, Unit> {

    data class Args(val driveId: String, val rating: Rating)

    // Both exceptions are outcomes the rating is kept for, not failures to report: offline it waits, not ratable it's done
    @Suppress("SwallowedException")
    override suspend fun build(args: Args) {
        openDriveQuestion.clear(args.driveId)
        driveReminders.cancel(args.driveId)
        tripHistoryCache.rate(args.driveId, args.rating)
        val isStartedDrive = startedDrives.all().any { it.id == args.driveId }
        if (isStartedDrive) startedDrives.rate(args.driveId, args.rating)
        try {
            driveRater.rate(args.driveId, args.rating)
        } catch (error: OfflineException) {
            return
        } catch (error: DriveNotRatableException) {
            // Marked sent below, as retrying can't succeed
        }
        tripHistoryCache.markRatingSent(args.driveId, args.rating)
        if (isStartedDrive) startedDrives.markRatingSent(args.driveId, args.rating)
    }
}
