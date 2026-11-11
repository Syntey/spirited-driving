package com.syntey.spiriteddriving.android.tools.binding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermissionListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission

class PlatformLocationPermissionImpl(private val context: Context) : PlatformLocationPermission {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences by lazy { context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE) }
    private var activity: ComponentActivity? = null
    private var launcher: ActivityResultLauncher<Array<String>>? = null
    private val pendingListeners = mutableListOf<LocationPermissionListener>()

    /**
     * Lets the prompt show over [activity]. Call it from the activity's `onCreate`: the answer comes back through the
     * activity, which can only register for it before it starts. A recreated activity gets the answer to a prompt shown
     * over the one before it.
     */
    fun attach(activity: ComponentActivity) {
        this.activity = activity
        launcher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { onAnswered() }
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (this@PlatformLocationPermissionImpl.activity === activity) {
                        this@PlatformLocationPermissionImpl.activity = null
                        launcher = null
                    }
                }
            },
        )
    }

    override fun request(listener: LocationPermissionListener) {
        mainHandler.post {
            val launcher = launcher
            if (launcher == null || isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                listener.onResult(grantedAccess() ?: LocationAccess.Denied)
                return@post
            }
            // Also counts a denial made in the system settings.
            if (showsRationale() == true) rememberDenial()
            pendingListeners += listener
            // A request made while the prompt is showing waits for the same answer.
            if (pendingListeners.size == 1) {
                // Asking with approximate access granted offers to upgrade it to precise. When the user has denied for good,
                // the system answers at once without a prompt.
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    // The system asks for a rationale after the first denial, until the user denies for good. Without one after a prompt,
    // the user either denied for good or, before any denial, only closed the prompt: a denial seen before tells them apart.
    // It's remembered even when nobody waits for the answer, as after the process was killed during the prompt.
    private fun onAnswered() {
        val rationale = showsRationale()
        if (rationale == true) rememberDenial()
        val access = grantedAccess()
            ?: if (rationale == false && wasDenied()) LocationAccess.DeniedForGood else LocationAccess.Denied
        val listeners = pendingListeners.toList()
        pendingListeners.clear()
        listeners.forEach { it.onResult(access) }
    }

    private fun grantedAccess(): LocationAccess? = when {
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> LocationAccess.Precise
        isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) -> LocationAccess.Approximate
        else -> null
    }

    // Null without an activity to ask.
    private fun showsRationale(): Boolean? =
        activity?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun rememberDenial() = preferences.edit { putBoolean(KEY_DENIED, true) }

    private fun wasDenied() = preferences.getBoolean(KEY_DENIED, false)

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val PREFERENCES_NAME = "location_permission"
        const val KEY_DENIED = "denied"
    }
}
