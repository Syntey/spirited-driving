package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakeRecentPlaces
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRecentPlacesUseCaseTest {

    @Test
    fun `returns the kept places`() = runTest {
        val kept = listOf(place("Karlštejn", 49.9338), place("Dresden", 51.0504))

        assertEquals(kept, GetRecentPlacesUseCase(FakeRecentPlaces(kept)).build(Unit))
    }

    private fun place(name: String, latitude: Double) =
        Place(name = name, area = null, location = GeoPoint(latitude = latitude, longitude = 14.1832917))
}
