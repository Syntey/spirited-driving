package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.LocationPermissionListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RequestPreciseLocationUseCaseTest {

    @Test
    fun `returns what the user allows in the precise prompt`() = runTest {
        val useCase = RequestPreciseLocationUseCase(LocationPermission(PrecisePermission(LocationAccess.Precise)))

        assertEquals(LocationAccess.Precise, useCase.build(Unit))
    }

    private class PrecisePermission(private val access: LocationAccess) : PlatformLocationPermission {
        override fun request(listener: LocationPermissionListener) = Unit
        override fun requestPrecise(listener: LocationPermissionListener) = listener.onResult(access)
        override fun currentAccess(listener: LocationPermissionListener) = Unit
        override fun openSettings() = Unit
    }
}
