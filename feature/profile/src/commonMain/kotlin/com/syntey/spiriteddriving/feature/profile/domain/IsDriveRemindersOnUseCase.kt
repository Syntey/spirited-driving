package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory

/** Reads whether the phone delivers the drive feedback notification now. */
@Factory
internal class IsDriveRemindersOnUseCase(private val driveReminders: DriveReminders) : UseCase<Unit, Boolean> {

    override suspend fun build(args: Unit): Boolean = driveReminders.isOn()
}
