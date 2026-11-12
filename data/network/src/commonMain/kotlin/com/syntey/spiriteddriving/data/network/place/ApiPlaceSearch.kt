package com.syntey.spiriteddriving.data.network.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class ApiPlaceSearch(private val api: SpiritedDrivingApi) : PlaceSearch {

    override suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion> =
        api.searchPlaces(q = text, lat = near?.latitude, lon = near?.longitude).getOrThrowDomainError().items.map { it.toDomain() }

    override suspend fun placeAt(point: GeoPoint): Place? {
        val result = api.reverseLookup(point.latitude, point.longitude)
        val httpError = (result as? NetworkResult.Failure)?.error as? NetworkError.HttpError
        // No place near the point is an answer, not a failure
        if (httpError?.statusCode == HttpStatusCode.NotFound.value) return null
        return result.getOrThrowDomainError().toDomain().place
    }
}
