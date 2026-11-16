package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import com.syntey.spiriteddriving.core.platform.binding.DriveRemindersListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OpenDriveReminderSettingsUseCaseTest {

    @Test
    fun `opens the phone's settings for the drive reminders through the binding`() = runTest {
        val reminders = SettingsReminders()

        OpenDriveReminderSettingsUseCase(DriveReminders(reminders)).build(Unit)

        assertTrue(reminders.settingsOpened)
    }

    private class SettingsReminders : PlatformDriveReminders {
        var settingsOpened = false

        override fun isOn(listener: DriveRemindersListener) = Unit
        override fun openSettings() {
            settingsOpened = true
        }
    }
}
