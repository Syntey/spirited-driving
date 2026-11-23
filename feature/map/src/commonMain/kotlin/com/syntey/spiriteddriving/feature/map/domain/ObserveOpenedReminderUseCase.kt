package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.annotation.Factory

/**
 * Emits the question of each drive whose notification the user taps, while that drive's question is still open. A tap on an
 * older drive's notification, whose question is gone, emits nothing.
 */
@Factory
internal class ObserveOpenedReminderUseCase(private val driveReminders: DriveReminders, private val openDriveQuestion: OpenDriveQuestion) :
    FlowUseCase<Unit, DriveQuestion> {

    override fun build(args: Unit): Flow<DriveQuestion> = driveReminders.opened.mapNotNull { driveId ->
        openDriveQuestion.get()?.takeIf { it.driveId == driveId }
    }
}
