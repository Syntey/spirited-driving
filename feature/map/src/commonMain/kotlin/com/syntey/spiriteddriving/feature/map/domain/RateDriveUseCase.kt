package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Keeps [Args.rating] on the drive and sends the drive as kept now. A thumb answers the question about the drive, so the
 * question is cleared and its notification cancelled. Throws `OfflineException` without a connection, leaving the rating to be
 * sent later. The UI calls it on each change of the thumb or its reasons.
 */
@Factory
internal class RateDriveUseCase(
    private val openDriveQuestion: OpenDriveQuestion,
    private val startedDrives: StartedDrives,
    private val driveSender: DriveSender,
    @Provided private val driveReminders: DriveReminders,
) : UseCase<RateDriveUseCase.Args, Unit> {

    data class Args(val driveId: String, val rating: Rating)

    override suspend fun build(args: Args) {
        openDriveQuestion.clear(args.driveId)
        driveReminders.cancel(args.driveId)
        startedDrives.rate(args.driveId, args.rating)
        startedDrives.all().firstOrNull { it.id == args.driveId }?.let { driveSender.send(it) }
    }
}
