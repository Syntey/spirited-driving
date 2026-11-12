package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.getOrThrow
import org.koin.core.annotation.Factory

@Factory
internal class ApiPlaceSearch(private val api: SpiritedDrivingApi) : PlaceSearch {

    override suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion> =
        api.searchPlaces(q = text, lat = near?.latitude, lon = near?.longitude).getOrThrow().items.map { it.toDomain() }
}
