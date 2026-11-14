package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

/**
 * Finds places by what the user typed, from the backend.
 */
interface PlaceSearch {

    /**
     * Suggestions for [text], best match first. [near], the user's location or the centre of the map, only orders places
     * with the same name, nearest first. Throws [OfflineException] without a connection.
     */
    suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion>
}
