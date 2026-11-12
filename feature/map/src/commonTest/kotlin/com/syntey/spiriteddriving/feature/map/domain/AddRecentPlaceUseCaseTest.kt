package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakeRecentPlaces
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AddRecentPlaceUseCaseTest {

    @Test
    fun `a new place goes first`() = runTest {
        val recentPlaces = FakeRecentPlaces(listOf(place("Dresden", 51.0504)))

        val result = AddRecentPlaceUseCase(recentPlaces).build(place("Karlštejn", 49.9338))

        assertEquals(listOf("Karlštejn", "Dresden"), result.map { it.name })
    }

    @Test
    fun `a place kept already moves to the top, replaced by the new one`() = runTest {
        val old = place("Karlštejn", 49.9338)
        val recentPlaces = FakeRecentPlaces(listOf(place("Dresden", 51.0504), old))
        val newer = place("Karlštejn", 49.9338 + 0.00045)

        val result = AddRecentPlaceUseCase(recentPlaces).build(newer)

        assertEquals(listOf(newer, place("Dresden", 51.0504)), result)
    }

    @Test
    fun `keeps at most 15 places, dropping the oldest`() = runTest {
        val kept = (1..15).map { place("Town $it", 49.0 + it) }
        val recentPlaces = FakeRecentPlaces(kept)

        val result = AddRecentPlaceUseCase(recentPlaces).build(place("Karlštejn", 49.9338))

        assertEquals(15, result.size)
        assertEquals("Karlštejn", result.first().name)
        assertFalse("Town 15" in result.map { it.name })
    }

    private fun place(name: String, latitude: Double) =
        Place(name = name, area = null, location = GeoPoint(latitude = latitude, longitude = 14.1832917))
}
