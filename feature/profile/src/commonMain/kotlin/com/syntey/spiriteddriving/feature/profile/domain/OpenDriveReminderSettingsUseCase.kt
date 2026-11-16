package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** Opens the phone's settings for the drive feedback notification. */
@Factory
internal class OpenDriveReminderSettingsUseCase(@Provided private val driveReminders: DriveReminders) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) = driveReminders.openSettings()
}
