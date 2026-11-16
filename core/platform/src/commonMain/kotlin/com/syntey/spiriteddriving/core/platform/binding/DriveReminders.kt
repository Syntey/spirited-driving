package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [PlatformDriveReminders] for coroutines.
 */
class DriveReminders(private val binding: PlatformDriveReminders) {

    /** Whether the phone delivers the drive feedback notification now. */
    suspend fun isOn(): Boolean = suspendCancellableCoroutine { continuation ->
        binding.isOn(
            DriveRemindersListener { isOn ->
                if (continuation.isActive) continuation.resume(isOn)
            },
        )
    }

    /** Opens the phone's settings for the drive feedback notification. */
    fun openSettings() = binding.openSettings()
}
