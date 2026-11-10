package com.syntey.spiriteddriving.android

import android.app.Application
import com.syntey.spiriteddriving.android.tools.binding.PlatformBindingsImpl
import org.koin.android.ext.koin.androidContext
import timber.log.Timber
import com.syntey.spiriteddriving.shared.KmpApplication as SharedApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeKmp()
        initializeLogging()
    }

    private fun initializeKmp() {
        SharedApp.initializeSharedApplication(
            platformBindings = PlatformBindingsImpl(applicationContext = this),
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
