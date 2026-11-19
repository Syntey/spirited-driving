package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.LocationFix
import com.syntey.spiriteddriving.core.platform.binding.LocationUpdateListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation

/**
 * [PlatformLocation] that sends the fixes a test gives it with [emit], while updates are running.
 */
internal class FakePlatformLocation : PlatformLocation {

    private var listener: LocationUpdateListener? = null

    override fun startUpdates(listener: LocationUpdateListener) {
        this.listener = listener
    }

    override fun stopUpdates() {
        listener = null
    }

    /** Sends [fix] when updates are running; nothing otherwise, as the system sends nothing without access. */
    fun emit(fix: LocationFix) {
        listener?.onLocation(fix)
    }
}
