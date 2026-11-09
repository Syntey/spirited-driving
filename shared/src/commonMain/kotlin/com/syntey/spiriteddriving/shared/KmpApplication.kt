package com.syntey.spiriteddriving.shared

import com.syntey.spiriteddriving.shared.crashreporting.AppCrashReporting
import com.syntey.spiriteddriving.shared.crashreporting.CrashlyticsReporter
import com.syntey.spiriteddriving.shared.injection.AppInjection
import com.syntey.spiriteddriving.shared.logging.AppLogging
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
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
