package com.syntey.spiriteddriving.feature.onboarding.domain

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
        val useCase = RequestLocationAccessUseCase(LocationPermission(AnsweringPermission(LocationAccess.Approximate)))

        assertEquals(LocationAccess.Approximate, useCase.build(Unit))
    }

    @Test
    fun `a prompt that answers twice returns the first answer`() = runTest {
        val permission = object : PlatformLocationPermission {
            override fun request(listener: LocationPermissionListener) {
                listener.onResult(LocationAccess.Denied)
                listener.onResult(LocationAccess.Precise)
            }
            override fun currentAccess(listener: LocationPermissionListener) = Unit
            override fun openSettings() = Unit
        }

        assertEquals(LocationAccess.Denied, RequestLocationAccessUseCase(LocationPermission(permission)).build(Unit))
    }

    private class AnsweringPermission(private val access: LocationAccess) : PlatformLocationPermission {
        override fun request(listener: LocationPermissionListener) = listener.onResult(access)
        override fun currentAccess(listener: LocationPermissionListener) = Unit
        override fun openSettings() = Unit
    }
}
