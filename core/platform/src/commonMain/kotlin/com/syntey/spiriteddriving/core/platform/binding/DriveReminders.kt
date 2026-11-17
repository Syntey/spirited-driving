package com.syntey.spiriteddriving.core.platform.binding

import kotlin.time.Instant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [PlatformDriveReminders] for coroutines.
 */
class DriveReminders(private val binding: PlatformDriveReminders) {

    /** Whether the phone delivers the drive feedback notification now. */
    suspend fun isOn(): Boolean = suspendCancellableCoroutine { continuation ->
        binding.isOn { isOn ->
            if (continuation.isActive) continuation.resume(isOn)
        }
    }

    /** Opens the phone's settings for the drive feedback notification. */
    fun openSettings() = binding.openSettings()

    /** Schedules the drive feedback notification about [driveId] for [at], titled with [destinationName] when it's set. */
    fun schedule(driveId: String, destinationName: String?, at: Instant) {
        binding.schedule(driveId, destinationName, at.toEpochMilliseconds())
    }

    /** Removes the drive feedback notification about [driveId], scheduled or delivered. */
    fun cancel(driveId: String) = binding.cancel(driveId)

    /** The IDs of drives whose notification the user tapped. The listener is removed when the collection stops. */
    val opened: Flow<String> = callbackFlow {
        binding.setOpenListener { driveId -> trySend(driveId) }
        awaitClose { binding.setOpenListener(null) }
    }

    /** Whether the app may offer the drive feedback notification: the phone still lets it ask for the permission. */
    suspend fun canOffer(): Boolean = suspendCancellableCoroutine { continuation ->
        binding.canOffer { canOffer ->
            if (continuation.isActive) continuation.resume(canOffer)
        }
    }
}
