package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

/**
 * [PlaceSearch] that answers every search with [suggestions] and every point with [placeAt]. Records the searches and
 * points it got.
 */
class FakePlaceSearch(private val suggestions: List<PlaceSuggestion> = Recorded, private val placeAt: Place? = RecordedPlaceAt) :
    PlaceSearch {

    val searches: List<Search>
        field = mutableListOf<Search>()

    val lookups: List<GeoPoint>
        field = mutableListOf<GeoPoint>()

    override suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion> {
        searches += Search(text, near)
        return suggestions
    }

    override suspend fun placeAt(point: GeoPoint): Place? {
        lookups += point
        return placeAt
    }

    /** A search [FakePlaceSearch] got. */
    data class Search(val text: String, val near: GeoPoint?)

    companion object {
        /** The backend's suggestions for "karlst" near Prague, and Dresden, which isn't covered. */
        val Recorded: List<PlaceSuggestion> = listOf(
            PlaceSuggestion(
                place = Place(
                    name = "Karlštejn",
                    area = "okres Beroun, Central Bohemian Region",
                    location = GeoPoint(latitude = 49.933809, longitude = 14.1832917),
                ),
                isCovered = true,
                kind = PlaceKind.Town,
            ),
            PlaceSuggestion(
                place = Place(
                    name = "Karlštejn",
                    area = "Budňany, Central Bohemian Region",
                    location = GeoPoint(latitude = 49.939361, longitude = 14.1882543),
                ),
                isCovered = true,
                kind = PlaceKind.PointOfInterest,
            ),
            PlaceSuggestion(
                place = Place(
                    name = "Karlštejnská",
                    area = "Prague",
                    location = GeoPoint(latitude = 50.0552885, longitude = 14.3641761),
                ),
                isCovered = true,
                kind = PlaceKind.Street,
            ),
            PlaceSuggestion(
                place = Place(
                    name = "Dresden",
                    area = "Saxony",
                    location = GeoPoint(latitude = 51.0504, longitude = 13.7373),
                ),
                isCovered = false,
                kind = PlaceKind.Town,
            ),
        )

        /** The backend's name for a point at Karlštejn Castle. */
        val RecordedPlaceAt: Place = Place(
            name = "Karlštejn",
            area = "Budňany, Středočeský kraj",
            location = GeoPoint(latitude = 49.9394, longitude = 14.1882),
        )
    }
}
