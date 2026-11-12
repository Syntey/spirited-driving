package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.LocationPermissionListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLocationAccessUseCaseTest {

    @Test
    fun `returns the access the user allows now`() = runTest {
        val useCase = GetLocationAccessUseCase(LocationPermission(CurrentPermission(LocationAccess.Approximate)))

        assertEquals(LocationAccess.Approximate, useCase.build(Unit))
    }

    private class CurrentPermission(private val access: LocationAccess) : PlatformLocationPermission {
        override fun request(listener: LocationPermissionListener) = Unit
        override fun requestPrecise(listener: LocationPermissionListener) = Unit
        override fun currentAccess(listener: LocationPermissionListener) = listener.onResult(access)
        override fun openSettings() = Unit
    }
}
