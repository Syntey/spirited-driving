package com.syntey.spiriteddriving.core.platform.injection

import com.syntey.spiriteddriving.core.platform.binding.AppCheck
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.MapsApp
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation
import com.syntey.spiriteddriving.core.platform.binding.PlatformAppCheck
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp
import com.syntey.spiriteddriving.core.platform.binding.PlatformWebBrowser
import com.syntey.spiriteddriving.core.platform.binding.UserLocation
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

fun platformModule(
    platformBindings: PlatformBindings,
) = module {
    single { platformBindings.platform() }.withOptions { bind<Platform>() }
    single<PlatformLocationPermission> { platformBindings.locationPermission() }
    factory { LocationPermission(get()) }
    single<PlatformLocation> { platformBindings.location() }
    factory { UserLocation(get()) }
    single<PlatformDriveReminders> { platformBindings.driveReminders() }
    factory { DriveReminders(get()) }
    single<PlatformMapsApp> { platformBindings.mapsApp() }
    factory { MapsApp(get()) }
    single<PlatformWebBrowser> { platformBindings.webBrowser() }
    single<PlatformAuth> { platformBindings.auth() }
    single { Auth(get()) }
    single<PlatformAppCheck> { platformBindings.appCheck() }
    single { AppCheck(get()) }
}
