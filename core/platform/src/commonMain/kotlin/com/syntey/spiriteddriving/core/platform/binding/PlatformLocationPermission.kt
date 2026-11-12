package com.syntey.spiriteddriving.core.platform.binding

/**
 * The system's permission to use the location while the app is in use. Guidance runs in the maps app, so the app never
 * asks for location in the background.
 */
interface PlatformLocationPermission {

    /**
     * Shows the system prompt for location access and reports what the user allowed to [listener], once. When the system
     * no longer lets the app ask, because the user already answered or turned it off for good, it reports the current
     * access without a prompt. A call made while the prompt is showing gets the same answer. Can be called from any
     * thread; [listener] may be called on any thread.
     */
    fun request(listener: LocationPermissionListener)

    /**
     * Reports what the user allows now to [listener], without a prompt: [LocationAccess.Denied] until they're first asked.
     * Can be called from any thread; [listener] may be called on any thread.
     */
    fun currentAccess(listener: LocationPermissionListener)

    /**
     * Opens this app's page in the system settings, where the user can turn location access on after denying it for good.
     * Can be called from any thread.
     */
    fun openSettings()
}

interface LocationPermissionListener {
    fun onResult(access: LocationAccess)
}

/**
 * How much of the user's location the app may use.
 */
enum class LocationAccess {
    /** The exact location, so routes start on the road the user is on. */
    Precise,

    /** Only the rough area. */
    Approximate,

    /** None; the app may ask again. */
    Denied,

    /** None, and the system won't show the prompt again: only the system settings can change it. */
    DeniedForGood,
}
