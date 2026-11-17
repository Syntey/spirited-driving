package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Marks the drive [String] as not driven, which clears the question about it and cancels its notification, and sends the drive
 * as kept now, so the backend removes it. Throws `OfflineException` without a connection, leaving the removal for later.
 */
@Factory
internal class MarkNotDrivenUseCase(
    private val openDriveQuestion: OpenDriveQuestion,
    private val startedDrives: StartedDrives,
    private val driveSender: DriveSender,
    @Provided private val driveReminders: DriveReminders,
) : UseCase<String, Unit> {

    override suspend fun build(args: String) {
        openDriveQuestion.clear(args)
        driveReminders.cancel(args)
        startedDrives.markNotDriven(args)
        startedDrives.all().firstOrNull { it.id == args }?.let { driveSender.send(it) }
    }
}
