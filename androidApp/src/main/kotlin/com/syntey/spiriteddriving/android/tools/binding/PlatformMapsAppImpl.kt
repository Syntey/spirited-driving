package com.syntey.spiriteddriving.android.tools.binding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.syntey.spiriteddriving.core.platform.binding.MapsAppKind
import com.syntey.spiriteddriving.core.platform.binding.MapsAppListener
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp

class PlatformMapsAppImpl(private val context: Context) : PlatformMapsApp {

    override val kind: MapsAppKind = MapsAppKind.GoogleMaps

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun open(link: String, listener: MapsAppListener) {
        mainHandler.post {
            // Pinned to Google Maps, so a missing or turned off Google Maps fails here instead of the link opening in a browser

            val opened = start(Intent(Intent.ACTION_VIEW, link.toUri()).setPackage(PACKAGE))
            listener.onResult(if (opened) MapsAppResult.Opened else MapsAppResult.Missing)
        }
    }

    override fun openStore() {
        mainHandler.post {
            // Without the Play Store app, the web page opens instead.
            start(Intent(Intent.ACTION_VIEW, "market://details?id=$PACKAGE".toUri())) ||
                start(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$PACKAGE".toUri()))
        }
    }

    private fun start(intent: Intent): Boolean {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private companion object {
        const val PACKAGE = "com.google.android.apps.maps"
    }
}
