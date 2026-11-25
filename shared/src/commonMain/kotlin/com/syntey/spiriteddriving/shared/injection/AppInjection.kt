package com.syntey.spiriteddriving.shared.injection

import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
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
                module { single { platformBindings } },
            )
        }
    }
}
