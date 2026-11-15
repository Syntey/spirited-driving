package com.syntey.spiriteddriving.android

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.syntey.spiriteddriving.android.tools.appCheckProviderFactory
import com.syntey.spiriteddriving.android.tools.binding.PlatformBindingsImpl
import org.koin.android.ext.koin.androidContext
import timber.log.Timber
import com.syntey.spiriteddriving.shared.KmpApplication as SharedApp

class App : Application() {

    val platformBindings by lazy { PlatformBindingsImpl(applicationContext = this) }

    override fun onCreate() {
        super.onCreate()
        initializeAppCheck()
        initializeKmp()
        initializeLogging()
    }

    private fun initializeAppCheck() {
        // Without google-services.json Firebase isn't initialised, and App Check has nothing to install on.
        if (FirebaseApp.getApps(this).isEmpty()) return
        Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory())
    }

    private fun initializeKmp() {
        SharedApp.initializeSharedApplication(
            platformBindings = platformBindings,
            isDebugBuild = BuildConfig.DEBUG,
        ) {
            androidContext(this@App)
        }
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
