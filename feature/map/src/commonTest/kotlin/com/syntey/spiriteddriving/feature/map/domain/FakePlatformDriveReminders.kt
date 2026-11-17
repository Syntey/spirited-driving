package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.DriveReminderOpenListener
import com.syntey.spiriteddriving.core.platform.binding.DriveRemindersListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders

/**
 * [PlatformDriveReminders] that records the scheduled and cancelled notifications, and lets a test tap one with [tap].
 */
internal class FakePlatformDriveReminders(var canOffer: Boolean = false) : PlatformDriveReminders {

    val scheduled = mutableListOf<Scheduled>()
    val cancelled = mutableListOf<String>()
    var settingsOpened = false
    private var openListener: DriveReminderOpenListener? = null

    data class Scheduled(val driveId: String, val destinationName: String?, val atEpochMillis: Long)

    fun tap(driveId: String) {
        openListener?.onOpen(driveId)
    }

    override fun isOn(listener: DriveRemindersListener) = listener.onResult(false)
    override fun openSettings() {
        settingsOpened = true
    }
    override fun schedule(driveId: String, destinationName: String?, atEpochMillis: Long) {
        scheduled += Scheduled(driveId, destinationName, atEpochMillis)
    }
    override fun cancel(driveId: String) {
        cancelled += driveId
    }
    override fun setOpenListener(listener: DriveReminderOpenListener?) {
        openListener = listener
    }
    override fun canOffer(listener: DriveRemindersListener) = listener.onResult(canOffer)
}
