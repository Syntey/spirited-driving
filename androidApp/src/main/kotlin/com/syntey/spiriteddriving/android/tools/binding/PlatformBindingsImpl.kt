package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import androidx.activity.ComponentActivity
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.core.platform.binding.PlatformFirebaseCrashlytics
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp

class PlatformBindingsImpl(private val applicationContext: Context) : PlatformBindings {

    private val locationPermission = PlatformLocationPermissionImpl(applicationContext)
    private val location = PlatformLocationImpl(applicationContext)
    private val mapsApp = PlatformMapsAppImpl(applicationContext)

    override fun platform(): Platform = PlatformImpl(applicationContext)
    override fun firebaseCrashlytics(): PlatformFirebaseCrashlytics = PlatformFirebaseCrashlyticsImpl()
    override fun locationPermission(): PlatformLocationPermission = locationPermission
    override fun location(): PlatformLocation = location
    override fun mapsApp(): PlatformMapsApp = mapsApp

    /**
     * Connects the bindings that show system UI, such as a permission prompt, to [activity]. Call it from `onCreate`.
     */
    fun attach(activity: ComponentActivity) {
        locationPermission.attach(activity)
    }
}
