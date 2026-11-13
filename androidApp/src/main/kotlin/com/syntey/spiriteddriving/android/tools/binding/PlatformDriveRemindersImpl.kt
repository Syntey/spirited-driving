package com.syntey.spiriteddriving.android.tools.binding

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.syntey.spiriteddriving.core.platform.binding.DriveRemindersListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders
import com.syntey.spiriteddriving.core.resources.*

class PlatformDriveRemindersImpl(private val context: Context) : PlatformDriveReminders {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences by lazy { context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE) }
    private var activity: ComponentActivity? = null
    private var launcher: ActivityResultLauncher<String>? = null

    /**
     * Lets the notification permission prompt show over [activity]. Call it from the activity's `onCreate`, as the prompt's
     * answer comes back through the activity, which can only register for it before it starts.
     */
    fun attach(activity: ComponentActivity) {
        this.activity = activity
        launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (this@PlatformDriveRemindersImpl.activity === activity) {
                        this@PlatformDriveRemindersImpl.activity = null
                        launcher = null
                    }
                }
            },
        )
    }

    override fun isOn(listener: DriveRemindersListener) {
        mainHandler.post {
            val channel = ensureChannel()
            val on = NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                channel.importance != NotificationManager.IMPORTANCE_NONE &&
                (!needsPermission() || isGranted())
            listener.onResult(on)
        }
    }

    override fun openSettings() {
        mainHandler.post {
            if (needsPermission()) {
                val launcher = launcher
                // Once the system stops showing the prompt, after the user denied it for good, it answers at once.
                if (launcher == null || (wasAsked() && activity?.shouldShowRequestPermissionRationale(PERMISSION) == false)) {
                    startSettings(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                } else {
                    preferences.edit { putBoolean(KEY_ASKED, true) }
                    launcher.launch(PERMISSION)
                }
                return@post
            }
            ensureChannel()
            startSettings(
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID),
            )
        }
    }

    // Created again on each read, which keeps the channel listed in the system settings. Creating it again changes nothing
    // the user set.
    private fun ensureChannel(): NotificationChannel {
        val name = MR.strings.settings_drive_reminders.getString(context)
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        return manager.getNotificationChannel(CHANNEL_ID) ?: channel
    }

    private fun needsPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isGranted()

    private fun isGranted() = ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun wasAsked() = preferences.getBoolean(KEY_ASKED, false)

    private fun startSettings(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private companion object {
        /** The channel the drive feedback notification posts to. Its settings are where the user silences it. */
        const val CHANNEL_ID = "drive_reminders"
        const val PREFERENCES_NAME = "drive_reminders"
        const val KEY_ASKED = "asked"
        const val PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }
}
