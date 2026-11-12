package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
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
            kind = "TOWN",
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
            kind = "TOWN",
        ).toDomain()

        assertFalse(suggestion.isCovered)
    }

    @Test
    fun `a missing area maps to null`() {
        val suggestion = PlaceSuggestionDto(
            location = GeoPointDto(lat = 50.0, lon = 14.0),
            name = "Somewhere",
            covered = true,
            kind = "TOWN",
        ).toDomain()

        assertNull(suggestion.place.area)
    }

    @Test
    fun `TOWN maps to town`() {
        assertEquals(PlaceKind.Town, suggestion(kind = "TOWN").kind)
    }

    @Test
    fun `STREET maps to street`() {
        assertEquals(PlaceKind.Street, suggestion(kind = "STREET").kind)
    }

    @Test
    fun `ADDRESS maps to address`() {
        assertEquals(PlaceKind.Address, suggestion(kind = "ADDRESS").kind)
    }

    @Test
    fun `NATURE maps to nature`() {
        assertEquals(PlaceKind.Nature, suggestion(kind = "NATURE").kind)
    }

    @Test
    fun `POI maps to point of interest`() {
        assertEquals(PlaceKind.PointOfInterest, suggestion(kind = "POI").kind)
    }

    @Test
    fun `an unknown kind maps to a point of interest`() {
        assertEquals(PlaceKind.PointOfInterest, suggestion(kind = "SOMETHING_NEW").kind)
    }

    private fun suggestion(kind: String) = PlaceSuggestionDto(
        location = GeoPointDto(lat = 50.0, lon = 14.0),
        name = "Somewhere",
        covered = true,
        kind = kind,
    ).toDomain()
}
