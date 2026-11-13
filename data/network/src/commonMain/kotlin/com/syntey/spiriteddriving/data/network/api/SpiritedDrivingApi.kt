package com.syntey.spiriteddriving.data.network.api

import com.syntey.spiriteddriving.data.network.dto.coverage.CoverageDto
import com.syntey.spiriteddriving.data.network.dto.feedback.FeedbackRequestDto
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSearchResultDto
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSuggestionDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanRequestDto
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.Revalidated
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

/** The backend's endpoints, in the order of the API spec's paths. */
internal interface SpiritedDrivingApi {

    /** `POST /plans` (`planTrip`). */
    @POST("plans")
    suspend fun planTrip(
        @Body request: PlanRequestDto,
    ): NetworkResult<PlanDto>

    /** `GET /places/search` (`searchPlaces`). */
    @GET("places/search")
    suspend fun searchPlaces(
        @Query("q") q: String,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
    ): NetworkResult<PlaceSearchResultDto>

    /** `GET /places/reverse` (`reverseLookup`). */
    @GET("places/reverse")
    suspend fun reverseLookup(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): NetworkResult<PlaceSuggestionDto>

    /** `GET /coverage` (`getCoverage`). */
    @GET("coverage")
    suspend fun getCoverage(
        @Header("If-None-Match") ifNoneMatch: String? = null,
    ): NetworkResult<Revalidated<CoverageDto>>

    /** `POST /feedback` (`sendFeedback`). */
    @POST("feedback")
    suspend fun sendFeedback(
        @Body request: FeedbackRequestDto,
    ): NetworkResult<Unit>
}
