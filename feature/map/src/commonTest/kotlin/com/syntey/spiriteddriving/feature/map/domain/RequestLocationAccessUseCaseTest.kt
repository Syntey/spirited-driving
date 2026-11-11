package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.LocationPermissionListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RequestLocationAccessUseCaseTest {

    @Test
    fun `returns what the user allowed in the prompt`() = runTest {
        val useCase = RequestLocationAccessUseCase(LocationPermission(AnsweringPermission(LocationAccess.Precise)))

        assertEquals(LocationAccess.Precise, useCase.build(Unit))
    }

    private class AnsweringPermission(private val access: LocationAccess) : PlatformLocationPermission {
        override fun request(listener: LocationPermissionListener) = listener.onResult(access)
        override fun currentAccess(listener: LocationPermissionListener) = Unit
        override fun openSettings() = Unit
    }
}
