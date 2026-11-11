package com.syntey.spiriteddriving.core.platform.binding

/**
 * The user's location while the app is in use. Guidance runs in the maps app, so the app never asks for it in the
 * background.
 */
interface PlatformLocation {

    /**
     * Sends the user's location to [listener]: the last known one at once, when there is one, then each new one as the
     * user moves. Without location access it sends nothing. A second call replaces the listener. Can be called from any
     * thread; [listener] may be called on any thread.
     */
    fun startUpdates(listener: LocationUpdateListener)

    /** Stops the updates [startUpdates] started. Can be called from any thread. */
    fun stopUpdates()
}

interface LocationUpdateListener {
    fun onLocation(fix: LocationFix)
}

/** Where the user is, in WGS 84 degrees. */
data class LocationFix(val latitude: Double, val longitude: Double)
