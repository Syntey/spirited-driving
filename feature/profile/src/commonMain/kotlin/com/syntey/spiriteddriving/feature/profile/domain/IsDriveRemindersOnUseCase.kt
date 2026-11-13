package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** Reads whether the phone delivers the drive feedback notification now. */
@Factory
internal class IsDriveRemindersOnUseCase(@Provided private val driveReminders: DriveReminders) : UseCase<Unit, Boolean> {

    override suspend fun build(args: Unit): Boolean = driveReminders.isOn()
}
