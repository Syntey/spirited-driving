package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.LocationFix
import com.syntey.spiriteddriving.core.platform.binding.LocationUpdateListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocation
import com.syntey.spiriteddriving.core.platform.binding.UserLocation
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveUserLocationUseCaseTest {

    @Test
    fun `emits each location the binding sends as a point`() = runTest {
        val useCase = ObserveUserLocationUseCase(UserLocation(TwoFixesLocation()))

        assertEquals(
            listOf(GeoPoint(latitude = 50.08, longitude = 14.42), GeoPoint(latitude = 50.09, longitude = 14.43)),
            useCase.build(Unit).take(2).toList(),
        )
    }

    @Test
    fun `stops the updates once collection ends`() = runTest {
        val location = TwoFixesLocation()

        ObserveUserLocationUseCase(UserLocation(location)).build(Unit).take(2).toList()

        assertTrue(location.stopped)
    }

    private class TwoFixesLocation : PlatformLocation {
        var stopped = false

        override fun startUpdates(listener: LocationUpdateListener) {
            listener.onLocation(LocationFix(latitude = 50.08, longitude = 14.42))
            listener.onLocation(LocationFix(latitude = 50.09, longitude = 14.43))
        }

        override fun stopUpdates() {
            stopped = true
        }
    }
}
