package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Turns on the drive reminder from the offer: the phone shows its permission prompt where it still asks, else its settings.
 */
@Factory
internal class TurnOnRemindersUseCase(@Provided private val driveReminders: DriveReminders) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) = driveReminders.openSettings()
}
