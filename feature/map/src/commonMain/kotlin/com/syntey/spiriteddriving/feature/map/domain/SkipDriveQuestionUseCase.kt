package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Drops the question about the drive [String] without a rating, when the rating step is closed or the reminder bubble is
 * dismissed. Its notification is cancelled with it.
 */
@Factory
internal class SkipDriveQuestionUseCase(
    private val openDriveQuestion: OpenDriveQuestion,
    @Provided private val driveReminders: DriveReminders,
) : UseCase<String, Unit> {

    override suspend fun build(args: String) {
        openDriveQuestion.clear(args)
        driveReminders.cancel(args)
    }
}
