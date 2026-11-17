package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TurnOnRemindersUseCaseTest {

    @Test
    fun `turning on opens the phone's settings for the reminders`() = runTest {
        val reminders = FakePlatformDriveReminders()

        TurnOnRemindersUseCase(DriveReminders(reminders)).build(Unit)

        assertTrue(reminders.settingsOpened)
    }
}
