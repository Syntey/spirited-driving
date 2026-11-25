package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.koin.core.annotation.Factory

/**
 * [PlatformLocationPermission] for coroutines.
 */
@Factory
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

    /**
     * Offers precise location while only approximate is allowed, and returns what the user allows once they answer.
     */
    suspend fun requestPrecise(): LocationAccess = suspendCancellableCoroutine { continuation ->
        binding.requestPrecise(
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
