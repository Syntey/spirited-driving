package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationPermissionTest {

    @Test
    fun `request returns what the user allowed in the prompt`() = runTest {
        val fake = FakePlatformLocationPermission()

        assertEquals(LocationAccess.Approximate, LocationPermission(fake).request())
    }

    @Test
    fun `requestPrecise returns what the user allows after the offer`() = runTest {
        val fake = FakePlatformLocationPermission()

        assertEquals(LocationAccess.Precise, LocationPermission(fake).requestPrecise())
    }

    @Test
    fun `current returns the access without a prompt`() = runTest {
        val fake = FakePlatformLocationPermission()

        assertEquals(LocationAccess.DeniedForGood, LocationPermission(fake).current())
    }

    @Test
    fun `a listener called twice returns its first answer`() = runTest {
        val fake = FakePlatformLocationPermission(answersTwice = true)

        assertEquals(LocationAccess.Approximate, LocationPermission(fake).request())
    }
}

private class FakePlatformLocationPermission(private val answersTwice: Boolean = false) : PlatformLocationPermission {

    override fun request(listener: LocationPermissionListener) {
        listener.onResult(LocationAccess.Approximate)
        if (answersTwice) listener.onResult(LocationAccess.Denied)
    }

    override fun currentAccess(listener: LocationPermissionListener) = listener.onResult(LocationAccess.DeniedForGood)

    override fun requestPrecise(listener: LocationPermissionListener) = listener.onResult(LocationAccess.Precise)

    override fun openSettings() = Unit
}
