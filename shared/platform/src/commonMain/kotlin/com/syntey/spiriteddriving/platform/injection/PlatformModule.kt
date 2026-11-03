package com.syntey.spiriteddriving.platform.injection

import com.syntey.spiriteddriving.platform.binding.Platform
import com.syntey.spiriteddriving.platform.binding.PlatformBindings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

fun platformModule(
    platformBindings: PlatformBindings,
) = module {
    single { platformBindings.platform() }.withOptions { bind<Platform>() }
}
