package com.syntey.spiriteddriving.android.tools.binding

import android.Manifest
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
import androidx.work.WorkManager
import com.syntey.spiriteddriving.android.tools.binding.DriveReminderSupport.EXTRA_DRIVE_ID
import com.syntey.spiriteddriving.android.tools.binding.DriveReminderSupport.KEY_ASKED
import com.syntey.spiriteddriving.android.tools.binding.DriveReminderSupport.NOTIFICATION_ID
import com.syntey.spiriteddriving.core.platform.binding.DriveReminderOpenListener
import com.syntey.spiriteddriving.core.platform.binding.DriveRemindersListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders

class PlatformDriveRemindersImpl(private val context: Context) : PlatformDriveReminders {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences by lazy { DriveReminderSupport.preferences(context) }
    private var activity: ComponentActivity? = null
    private var launcher: ActivityResultLauncher<String>? = null

    // Read and written on the main thread only.
    private var openListener: DriveReminderOpenListener? = null
    private var pendingOpenDriveId: String? = null

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

    /**
     * Reads a tapped notification from [intent], an activity's launch or new intent, and opens its drive. The extra is
     * removed so that the same intent doesn't open it again.
     */
    fun onIntent(intent: Intent) {
        val driveId = intent.getStringExtra(EXTRA_DRIVE_ID) ?: return
        intent.removeExtra(EXTRA_DRIVE_ID)
        mainHandler.post {
            val listener = openListener
            if (listener == null) pendingOpenDriveId = driveId else listener.onOpen(driveId)
        }
    }

    override fun isOn(listener: DriveRemindersListener) {
        mainHandler.post {
            val channel = DriveReminderSupport.ensureChannel(context)
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
            DriveReminderSupport.ensureChannel(context)
            startSettings(
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, DriveReminderSupport.CHANNEL_ID),
            )
        }
    }

    override fun schedule(driveId: String, destinationName: String?, atEpochMillis: Long) {
        // One reminder at a time: a new drive's replaces the one before. WorkManager keeps it across a reboot, and may
        // deliver it some minutes late, later while the phone dozes.
        DriveReminderWorker.schedule(context, driveId, destinationName, atEpochMillis, driveTag(driveId))
    }

    override fun cancel(driveId: String) {
        NotificationManagerCompat.from(context).cancel(driveId, NOTIFICATION_ID)
        WorkManager.getInstance(context).cancelAllWorkByTag(driveTag(driveId))
    }

    override fun setOpenListener(listener: DriveReminderOpenListener?) {
        mainHandler.post {
            openListener = listener
            val driveId = pendingOpenDriveId
            if (listener != null && driveId != null) {
                pendingOpenDriveId = null
                listener.onOpen(driveId)
            }
        }
    }

    override fun canOffer(listener: DriveRemindersListener) {
        mainHandler.post { listener.onResult(needsPermission()) }
    }

    private fun driveTag(driveId: String) = "drive_reminder:$driveId"

    private fun needsPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isGranted()

    private fun isGranted() = ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun wasAsked() = preferences.getBoolean(KEY_ASKED, false)

    private fun startSettings(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private companion object {
        const val PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }
}
