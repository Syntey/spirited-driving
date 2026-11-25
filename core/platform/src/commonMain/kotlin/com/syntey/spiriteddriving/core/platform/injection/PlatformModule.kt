package com.syntey.spiriteddriving.core.platform.injection

import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.PlatformAppCheck
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.PlatformBindings
import com.syntey.spiriteddriving.core.platform.binding.PlatformDriveReminders
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp
import com.syntey.spiriteddriving.core.platform.binding.PlatformWebBrowser
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/** Hands out each native binding from [PlatformBindings]; the wrappers around them are found by the component scan. */
@Module
@ComponentScan("com.syntey.spiriteddriving.core.platform")
@Configuration
class PlatformModule {

    @Single
    fun platform(bindings: PlatformBindings): Platform = bindings.platform()

    @Single
    fun locationPermission(bindings: PlatformBindings): PlatformLocationPermission = bindings.locationPermission()

    @Single
    fun location(bindings: PlatformBindings): PlatformLocation = bindings.location()

    @Single
    fun driveReminders(bindings: PlatformBindings): PlatformDriveReminders = bindings.driveReminders()

    @Single
    fun mapsApp(bindings: PlatformBindings): PlatformMapsApp = bindings.mapsApp()

    @Single
    fun webBrowser(bindings: PlatformBindings): PlatformWebBrowser = bindings.webBrowser()

    @Single
    fun auth(bindings: PlatformBindings): PlatformAuth = bindings.auth()

    @Single
    fun appCheck(bindings: PlatformBindings): PlatformAppCheck = bindings.appCheck()
}
