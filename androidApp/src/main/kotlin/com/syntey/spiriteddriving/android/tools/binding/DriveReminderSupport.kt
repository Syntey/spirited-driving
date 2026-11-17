package com.syntey.spiriteddriving.android.tools.binding

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.syntey.spiriteddriving.core.resources.*

/** What the drive feedback notification shares between the binding and its worker. */
internal object DriveReminderSupport {

    /** The channel the drive feedback notification posts to. Its settings are where the user silences it. */
    const val CHANNEL_ID = "drive_reminders"
    const val PREFERENCES_NAME = "drive_reminders"
    const val KEY_ASKED = "asked"

    /** The notification's ID within its drive's tag. */
    const val NOTIFICATION_ID = 1

    const val EXTRA_DRIVE_ID = "drive_id"
    const val EXTRA_DESTINATION_NAME = "destination_name"

    fun preferences(context: Context): SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Created again on each read, which keeps the channel listed in the system settings. Creating it again changes nothing
    // the user set.
    fun ensureChannel(context: Context): NotificationChannel {
        val name = MR.strings.settings_drive_reminders.getString(context)
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        return manager.getNotificationChannel(CHANNEL_ID) ?: channel
    }
}
