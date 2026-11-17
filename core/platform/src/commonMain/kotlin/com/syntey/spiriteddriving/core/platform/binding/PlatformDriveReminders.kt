package com.syntey.spiriteddriving.core.platform.binding

/**
 * The phone's notification for the drive feedback, which asks how the route was after a drive. The app posts it at about
 * the planned arrival, and a tap on it opens the app on the rating step.
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

    /**
     * Schedules the drive feedback notification about [driveId] for [atEpochMillis], titled with [destinationName] (or
     * without a name when null). It replaces the one scheduled before, as only the newest drive is asked about. Where the
     * phone doesn't deliver it, nothing shows. On iOS, a user who hasn't answered yet gets provisional authorization first,
     * which shows no prompt. Can be called from any thread.
     */
    fun schedule(driveId: String, destinationName: String?, atEpochMillis: Long)

    /**
     * Removes the notification about [driveId], scheduled or delivered. Another drive's is left as it is. Can be called from
     * any thread.
     */
    fun cancel(driveId: String)

    /**
     * Sets the listener for a tapped notification, which gets its drive's ID; null removes it. A tap that comes while no
     * listener is set, such as the one that started the app, is kept and given to the next listener set. [listener] is
     * called on the main thread. Can be called from any thread.
     */
    fun setOpenListener(listener: DriveReminderOpenListener?)

    /**
     * Reports to [listener] whether the app may offer the notification: the phone still lets the app ask for the
     * permission (Android 13 and later, not granted yet). Always false on iOS, where provisional authorization needs no
     * offer. Can be called from any thread; [listener] may be called on any thread.
     */
    fun canOffer(listener: DriveRemindersListener)
}

fun interface DriveRemindersListener {
    fun onResult(isOn: Boolean)
}

fun interface DriveReminderOpenListener {
    fun onOpen(driveId: String)
}
