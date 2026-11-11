package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.LocationPermissionListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OpenLocationSettingsUseCaseTest {

    @Test
    fun `opens the system settings through the binding`() = runTest {
        val permission = SettingsPermission()

        OpenLocationSettingsUseCase(LocationPermission(permission)).build(Unit)

        assertTrue(permission.settingsOpened)
    }

    private class SettingsPermission : PlatformLocationPermission {
        var settingsOpened = false

        override fun request(listener: LocationPermissionListener) = Unit
        override fun currentAccess(listener: LocationPermissionListener) = Unit
        override fun openSettings() {
            settingsOpened = true
        }
    }
}
