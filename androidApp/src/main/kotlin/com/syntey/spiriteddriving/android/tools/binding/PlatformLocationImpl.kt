package com.syntey.spiriteddriving.android.tools.binding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.syntey.spiriteddriving.core.platform.binding.LocationFix
import com.syntey.spiriteddriving.core.platform.binding.LocationUpdateListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation

class PlatformLocationImpl(context: Context) : PlatformLocation {

    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var callback: LocationListener? = null

    override fun startUpdates(listener: LocationUpdateListener) {
        mainHandler.post {
            stopCurrentUpdates()
            if (!hasLocationAccess()) return@post
            val locationListener = LocationListener { location ->
                listener.onLocation(LocationFix(location.latitude, location.longitude))
            }
            callback = locationListener
            requestUpdates(listener, locationListener)
        }
    }

    override fun stopUpdates() {
        mainHandler.post { stopCurrentUpdates() }
    }

    // Access is checked above; the client throws without it
    @SuppressLint("MissingPermission")
    // Nothing is logged: without access there's nothing to report, and the next start asks again
    @Suppress("SwallowedException")
    private fun requestUpdates(listener: LocationUpdateListener, locationListener: LocationListener) {
        try {
            client.lastLocation.addOnSuccessListener { location ->
                if (location != null && callback === locationListener) {
                    listener.onLocation(LocationFix(location.latitude, location.longitude))
                }
            }
            client.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS).build(),
                locationListener,
                Looper.getMainLooper(),
            )
        } catch (e: SecurityException) {
            // Access was revoked between the check and the call: there's nothing to send
        }
    }

    private fun stopCurrentUpdates() {
        callback?.let(client::removeLocationUpdates)
        callback = null
    }

    // Approximate access is enough to start: the fused provider coarsens the location itself
    private fun hasLocationAccess(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val UPDATE_INTERVAL_MS = 1_000L
    }
}
