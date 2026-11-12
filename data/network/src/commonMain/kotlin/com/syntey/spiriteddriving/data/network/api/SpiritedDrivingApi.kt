package com.syntey.spiriteddriving.data.network.api

import com.syntey.spiriteddriving.data.network.dto.coverage.CoverageDto
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSearchResultDto
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.Revalidated
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query

/** The backend's endpoints, in the order of the API spec's paths. */
internal interface SpiritedDrivingApi {

    /** `GET /places/search` (`searchPlaces`). */
    @GET("places/search")
    suspend fun searchPlaces(
        @Query("q") q: String,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
    ): NetworkResult<PlaceSearchResultDto>

    /** `GET /coverage` (`getCoverage`). */
    @GET("coverage")
    suspend fun getCoverage(
        @Header("If-None-Match") ifNoneMatch: String? = null,
    ): NetworkResult<Revalidated<CoverageDto>>
}
