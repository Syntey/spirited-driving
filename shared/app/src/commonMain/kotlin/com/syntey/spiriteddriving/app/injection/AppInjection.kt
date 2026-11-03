package com.syntey.spiriteddriving.app.injection

import com.syntey.spiriteddriving.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.platform.injection.platformModule
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin

/**
 * Injection entry-point.
 * This object initializes dependency injection in application.
 */
@KoinApplication
internal object AppInjection {

    fun initializeInjection(
        platformBindings: PlatformBindings,
        appDeclaration: KoinAppDeclaration?,
    ) {
        startKoin<AppInjection> {
            if (appDeclaration != null) {
                appDeclaration()
            }

            modules(
                platformModule(platformBindings),
            )
        }
    }
}
