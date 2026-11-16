package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import com.syntey.spiriteddriving.core.platform.binding.DriveRemindersListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsDriveRemindersOnUseCaseTest {

    @Test
    fun `reports on when the phone delivers the drive reminders`() = runTest {
        val useCase = IsDriveRemindersOnUseCase(DriveReminders(AnsweringReminders(isOn = true)))

        assertTrue(useCase.build(Unit))
    }

    @Test
    fun `reports off when the phone doesn't deliver the drive reminders`() = runTest {
        val useCase = IsDriveRemindersOnUseCase(DriveReminders(AnsweringReminders(isOn = false)))

        assertFalse(useCase.build(Unit))
    }

    private class AnsweringReminders(private val isOn: Boolean) : PlatformDriveReminders {
        override fun isOn(listener: DriveRemindersListener) = listener.onResult(isOn)
        override fun openSettings() = Unit
    }
}
