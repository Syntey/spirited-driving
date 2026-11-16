package com.syntey.spiriteddriving.android.tools.binding

import android.content.ActivityNotFoundException
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.syntey.spiriteddriving.core.platform.binding.PlatformWebBrowser

class PlatformWebBrowserImpl : PlatformWebBrowser {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activity: ComponentActivity? = null

    /** Lets the Custom Tab open over [activity]. Call it from the activity's `onCreate`. */
    fun attach(activity: ComponentActivity) {
        this.activity = activity
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (this@PlatformWebBrowserImpl.activity === activity) {
                        this@PlatformWebBrowserImpl.activity = null
                    }
                }
            },
        )
    }

    override fun open(url: String) {
        mainHandler.post {
            val activity = activity ?: return@post
            try {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(activity, url.toUri())
            } catch (_: ActivityNotFoundException) {
                // The phone has no browser that can show a Custom Tab, so the page doesn't open.
            }
        }
    }
}
