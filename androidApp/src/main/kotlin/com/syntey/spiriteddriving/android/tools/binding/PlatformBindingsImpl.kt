package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.PlatformAppCheck
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders
import com.syntey.spiriteddriving.core.platform.binding.PlatformFirebaseCrashlytics
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp
import com.syntey.spiriteddriving.core.platform.binding.PlatformWebBrowser

class PlatformBindingsImpl(private val applicationContext: Context) : PlatformBindings {

    private val locationPermission = PlatformLocationPermissionImpl(applicationContext)
    private val location = PlatformLocationImpl(applicationContext)
    private val driveReminders = PlatformDriveRemindersImpl(applicationContext)
    private val mapsApp = PlatformMapsAppImpl(applicationContext)
    private val webBrowser = PlatformWebBrowserImpl()
    private val auth = PlatformAuthImpl(applicationContext)
    private val appCheck = PlatformAppCheckImpl(applicationContext)
    private val firebaseCrashlytics = PlatformFirebaseCrashlyticsImpl(applicationContext)

    override fun platform(): Platform = PlatformImpl(applicationContext)
    override fun firebaseCrashlytics(): PlatformFirebaseCrashlytics = firebaseCrashlytics
    override fun locationPermission(): PlatformLocationPermission = locationPermission
    override fun location(): PlatformLocation = location
    override fun driveReminders(): PlatformDriveReminders = driveReminders
    override fun mapsApp(): PlatformMapsApp = mapsApp
    override fun webBrowser(): PlatformWebBrowser = webBrowser
    override fun auth(): PlatformAuth = auth
    override fun appCheck(): PlatformAppCheck = appCheck

    /**
     * Connects the bindings that show system UI, such as a permission prompt, to [activity]. Call it from `onCreate`.
     */
    fun attach(activity: ComponentActivity) {
        locationPermission.attach(activity)
        driveReminders.attach(activity)
        webBrowser.attach(activity)
        auth.attach(activity)
    }

    /** Passes an activity's launch or new intent on, so a tapped drive feedback notification opens its drive. */
    fun onIntent(intent: Intent) {
        driveReminders.onIntent(intent)
    }
}
