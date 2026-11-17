package com.syntey.spiriteddriving.android.tools.binding

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.syntey.spiriteddriving.android.MainActivity
import com.syntey.spiriteddriving.android.R
import com.syntey.spiriteddriving.android.tools.binding.DriveReminderSupport.EXTRA_DESTINATION_NAME
import com.syntey.spiriteddriving.android.tools.binding.DriveReminderSupport.EXTRA_DRIVE_ID
import com.syntey.spiriteddriving.android.tools.binding.DriveReminderSupport.NOTIFICATION_ID
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.format
import java.util.concurrent.TimeUnit

/** Posts the drive feedback notification when its time comes. */
class DriveReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val driveId = inputData.getString(EXTRA_DRIVE_ID) ?: return Result.success()
        val destinationName = inputData.getString(EXTRA_DESTINATION_NAME)
        DriveReminderSupport.ensureChannel(applicationContext)
        if (mayPost(applicationContext) && !isAppInFront()) post(applicationContext, driveId, destinationName)
        return Result.success()
    }

    // The app asks on its own while it's in front, and the notification would only repeat it.
    private fun isAppInFront(): Boolean {
        val process = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(process)
        return process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    // Only called once mayPost has checked the permission, which lint can't follow.
    @SuppressLint("MissingPermission")
    private fun post(context: Context, driveId: String, destinationName: String?) {
        NotificationManagerCompat.from(context).notify(driveId, NOTIFICATION_ID, notification(context, driveId, destinationName))
    }

    private fun mayPost(context: Context): Boolean {
        val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permitted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun notification(context: Context, driveId: String, destinationName: String?): Notification {
        val title = if (destinationName != null) {
            MR.strings.drive_feedback_question.format(destinationName).toString(context)
        } else {
            MR.strings.drive_feedback_question_unnamed.getString(context)
        }
        return NotificationCompat.Builder(context, DriveReminderSupport.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(MR.strings.drive_feedback_notification_body.getString(context))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openIntent(context, driveId))
            .build()
    }

    private fun openIntent(context: Context, driveId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_DRIVE_ID, driveId)
        return PendingIntent.getActivity(
            context,
            OPEN_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        private const val OPEN_REQUEST_CODE = 1

        /** The unique work that holds the scheduled reminder. */
        private const val WORK_NAME = "drive_reminder"

        fun schedule(context: Context, driveId: String, destinationName: String?, atEpochMillis: Long, tag: String) {
            val request = OneTimeWorkRequestBuilder<DriveReminderWorker>()
                .setInitialDelay((atEpochMillis - System.currentTimeMillis()).coerceAtLeast(0), TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(EXTRA_DRIVE_ID to driveId, EXTRA_DESTINATION_NAME to destinationName))
                .addTag(tag)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
