package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.platform.binding.DriveReminderOpenListener
import com.syntey.spiriteddriving.core.platform.binding.DriveRemindersListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders

/** [PlatformDriveReminders] that records the cancelled notifications and does nothing else. */
internal class FakePlatformDriveReminders : PlatformDriveReminders {

    val cancelled = mutableListOf<String>()

    override fun isOn(listener: DriveRemindersListener) = listener.onResult(false)
    override fun openSettings() = Unit
    override fun schedule(driveId: String, destinationName: String?, atEpochMillis: Long) = Unit
    override fun cancel(driveId: String) {
        cancelled += driveId
    }
    override fun setOpenListener(listener: DriveReminderOpenListener?) = Unit
    override fun canOffer(listener: DriveRemindersListener) = listener.onResult(false)
}
