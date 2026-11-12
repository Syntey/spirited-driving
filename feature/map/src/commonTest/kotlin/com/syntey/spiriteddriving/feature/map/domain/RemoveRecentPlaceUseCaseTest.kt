package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakeRecentPlaces
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveRecentPlaceUseCaseTest {

    @Test
    fun `removes only the place it's given`() = runTest {
        val karlstejn = place("Karlštejn", 49.9338)
        val recentPlaces = FakeRecentPlaces(listOf(karlstejn, place("Dresden", 51.0504)))

        val result = RemoveRecentPlaceUseCase(recentPlaces).build(karlstejn)

        assertEquals(listOf(place("Dresden", 51.0504)), result)
    }

    private fun place(name: String, latitude: Double) =
        Place(name = name, area = null, location = GeoPoint(latitude = latitude, longitude = 14.1832917))
}
