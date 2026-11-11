package com.syntey.spiriteddriving.core.platform.injection

import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

fun platformModule(
    platformBindings: PlatformBindings,
) = module {
    single { platformBindings.platform() }.withOptions { bind<Platform>() }
    single<PlatformLocationPermission> { platformBindings.locationPermission() }
    factory { LocationPermission(get()) }
}
