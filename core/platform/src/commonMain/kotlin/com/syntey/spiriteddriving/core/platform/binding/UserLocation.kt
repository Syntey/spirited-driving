package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * [PlatformLocation] as a flow.
 */
class UserLocation(private val binding: PlatformLocation) {

    /** The user's location while collected: updates start when collection starts and stop when it ends. */
    fun updates(): Flow<LocationFix> = callbackFlow {
        binding.startUpdates(
            object : LocationUpdateListener {
                override fun onLocation(fix: LocationFix) {
                    trySend(fix)
                }
            },
        )
        awaitClose { binding.stopUpdates() }
    }
}
