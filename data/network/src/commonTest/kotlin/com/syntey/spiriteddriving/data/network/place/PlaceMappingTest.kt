package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSuggestionDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class PlaceMappingTest {

    @Test
    fun `name area and location map to the place`() {
        val suggestion = PlaceSuggestionDto(
            location = GeoPointDto(lat = 49.933809, lon = 14.1832917),
            name = "Karlštejn",
            area = "okres Beroun, Central Bohemian Region",
            covered = true,
        ).toDomain()

        assertEquals(
            Place(
                name = "Karlštejn",
                area = "okres Beroun, Central Bohemian Region",
                location = GeoPoint(latitude = 49.933809, longitude = 14.1832917),
            ),
            suggestion.place,
        )
    }

    @Test
    fun `covered maps to isCovered`() {
        val suggestion = PlaceSuggestionDto(
            location = GeoPointDto(lat = 51.0504, lon = 13.7373),
            name = "Dresden",
            area = "Saxony",
            covered = false,
        ).toDomain()

        assertFalse(suggestion.isCovered)
    }

    @Test
    fun `a missing area maps to null`() {
        val suggestion = PlaceSuggestionDto(
            location = GeoPointDto(lat = 50.0, lon = 14.0),
            name = "Somewhere",
            covered = true,
        ).toDomain()

        assertNull(suggestion.place.area)
    }
}
