package com.syntey.spiriteddriving.core.platform.binding

/**
 * The phone's notification for the drive feedback, which asks how the route was after a drive. The app posts it; this
 * only reads whether the phone delivers it and opens the phone's settings for it.
 */
interface PlatformDriveReminders {

    /**
     * Reports to [listener] whether the phone delivers the drive feedback notification now, without a prompt. Can be called
     * from any thread; [listener] may be called on any thread.
     */
    fun isOn(listener: DriveRemindersListener)

    /**
     * Opens the phone's settings for the drive feedback notification. Where the system still lets the app ask for the
     * notification permission, it shows that prompt first. Can be called from any thread.
     */
    fun openSettings()
}

fun interface DriveRemindersListener {
    fun onResult(isOn: Boolean)
}
