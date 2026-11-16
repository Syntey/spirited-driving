package com.syntey.spiriteddriving.android.tools.binding

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.syntey.spiriteddriving.core.platform.binding.PlatformFirebaseCrashlytics

class PlatformFirebaseCrashlyticsImpl(private val context: Context) : PlatformFirebaseCrashlytics {

    override fun logMessage(message: String) {
        if (isConfigured()) Firebase.crashlytics.log(message)
    }

    override fun sendNonFatalException(error: Throwable) {
        if (isConfigured()) Firebase.crashlytics.recordException(error)
    }

    // Firebase isn't initialised without google-services.json, see the build's missingGoogleServicesStrategy.
    private fun isConfigured() = FirebaseApp.getApps(context).isNotEmpty()
}
