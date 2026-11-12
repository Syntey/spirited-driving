package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [PlatformLocationPermission] for coroutines.
 */
class LocationPermission(private val binding: PlatformLocationPermission) {

    /**
     * Shows the system prompt for location access, and returns what the user allowed once they answer.
     */
    suspend fun request(): LocationAccess = suspendCancellableCoroutine { continuation ->
        binding.request(
            object : LocationPermissionListener {
                override fun onResult(access: LocationAccess) {
                    if (continuation.isActive) continuation.resume(access)
                }
            },
        )
    }

    /** What the user allows now, without a prompt. */
    suspend fun current(): LocationAccess = suspendCancellableCoroutine { continuation ->
        binding.currentAccess(
            object : LocationPermissionListener {
                override fun onResult(access: LocationAccess) {
                    if (continuation.isActive) continuation.resume(access)
                }
            },
        )
    }

    /** Opens this app's page in the system settings. */
    fun openSettings() = binding.openSettings()
}
