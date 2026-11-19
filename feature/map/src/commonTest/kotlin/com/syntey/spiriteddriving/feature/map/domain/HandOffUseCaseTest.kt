package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.appleMapsLink
import com.syntey.spiriteddriving.core.domain.drive.googleMapsLink
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.MapsApp
import com.syntey.spiriteddriving.core.platform.binding.MapsAppKind
import com.syntey.spiriteddriving.core.platform.binding.MapsAppListener
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HandOffUseCaseTest {

    private val route = FakeRoutePlanner.Recorded.fastest

    @Test
    fun `on Android the Google Maps link is opened`() = runTest {
        val mapsApp = FakePlatformMapsApp(MapsAppKind.GoogleMaps, MapsAppResult.Opened)

        HandOffUseCase(MapsApp(mapsApp)).build(route)

        assertEquals(listOf(googleMapsLink(route)), mapsApp.links)
    }

    @Test
    fun `on iOS the Apple Maps link is opened`() = runTest {
        val mapsApp = FakePlatformMapsApp(MapsAppKind.AppleMaps, MapsAppResult.Opened)

        HandOffUseCase(MapsApp(mapsApp)).build(route)

        assertEquals(listOf(appleMapsLink(route)), mapsApp.links)
    }

    @Test
    fun `the result of opening the maps app is returned`() = runTest {
        val result = HandOffUseCase(MapsApp(FakePlatformMapsApp(MapsAppKind.GoogleMaps, MapsAppResult.Opened))).build(route)

        assertEquals(MapsAppResult.Opened, result)
    }

    @Test
    fun `a missing maps app is returned`() = runTest {
        val result = HandOffUseCase(MapsApp(FakePlatformMapsApp(MapsAppKind.GoogleMaps, MapsAppResult.Missing))).build(route)

        assertEquals(MapsAppResult.Missing, result)
    }
}
