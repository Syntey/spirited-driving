package com.syntey.spiriteddriving.app

import com.syntey.spiriteddriving.app.crashreporting.AppCrashReporting
import com.syntey.spiriteddriving.app.crashreporting.CrashlyticsReporter
import com.syntey.spiriteddriving.app.injection.AppInjection
import com.syntey.spiriteddriving.app.logging.AppLogging
import com.syntey.spiriteddriving.platform.binding.PlatformBindings
import org.koin.dsl.KoinAppDeclaration

/**
 * Shared KMP entrypoint.
 *
 * Initializes injection framework,
 */
object KmpApplication {

    fun initializeSharedApplication(
        platformBindings: PlatformBindings,
        isDebugBuild: Boolean,
        appDeclaration: KoinAppDeclaration? = null,
    ) {
        val crashlyticsReporter = CrashlyticsReporter(platformBindings.firebaseCrashlytics())

        AppInjection.initializeInjection(
            platformBindings = platformBindings,
            appDeclaration = appDeclaration,
        )
        AppCrashReporting.initialize(crashlyticsReporter)
        AppLogging.initialize(
            isDebugBuild = isDebugBuild,
            crashlytics = crashlyticsReporter,
        )
    }
}
