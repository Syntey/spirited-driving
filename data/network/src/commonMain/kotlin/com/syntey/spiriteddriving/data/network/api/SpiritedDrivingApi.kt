package com.syntey.spiriteddriving.data.network.api

import com.syntey.spiriteddriving.data.network.dto.coverage.CoverageDto
import com.syntey.spiriteddriving.data.network.dto.drive.ClaimDrivesRequestDto
import com.syntey.spiriteddriving.data.network.dto.drive.RatingInputDto
import com.syntey.spiriteddriving.data.network.dto.drive.RecordDriveRequestDto
import com.syntey.spiriteddriving.data.network.dto.feedback.FeedbackRequestDto
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSearchResultDto
import com.syntey.spiriteddriving.data.network.dto.place.PlaceSuggestionDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanDto
import com.syntey.spiriteddriving.data.network.dto.plan.PlanRequestDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.RenameSavedRouteRequestDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SaveRouteRequestDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteListDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SavedRouteSummaryDto
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.Revalidated
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
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

    /** `PUT /drives/{driveId}` (`recordDrive`). */
    @PUT("drives/{driveId}")
    suspend fun recordDrive(
        @Path("driveId") driveId: String,
        @Body request: RecordDriveRequestDto,
    ): NetworkResult<Unit>

    /** `DELETE /drives/{driveId}` (`deleteDrive`). */
    @DELETE("drives/{driveId}")
    suspend fun deleteDrive(
        @Path("driveId") driveId: String,
    ): NetworkResult<Unit>

    /** `PUT /drives/{driveId}/rating` (`rateDrive`). */
    @PUT("drives/{driveId}/rating")
    suspend fun rateDrive(
        @Path("driveId") driveId: String,
        @Body request: RatingInputDto,
    ): NetworkResult<Unit>

    /** `GET /me/saved-routes` (`listSavedRoutes`). */
    @GET("me/saved-routes")
    suspend fun listSavedRoutes(): NetworkResult<SavedRouteListDto>

    /** `POST /me/saved-routes` (`saveRoute`). */
    @POST("me/saved-routes")
    suspend fun saveRoute(
        @Body request: SaveRouteRequestDto,
    ): NetworkResult<SavedRouteSummaryDto>

    /** `GET /me/saved-routes/{savedRouteId}` (`getSavedRoute`). */
    @GET("me/saved-routes/{savedRouteId}")
    suspend fun getSavedRoute(
        @Path("savedRouteId") savedRouteId: String,
        @Query("maxHandOffWaypoints") maxHandOffWaypoints: Int,
    ): NetworkResult<SavedRouteDto>

    /** `PATCH /me/saved-routes/{savedRouteId}` (`renameSavedRoute`). */
    @PATCH("me/saved-routes/{savedRouteId}")
    suspend fun renameSavedRoute(
        @Path("savedRouteId") savedRouteId: String,
        @Body request: RenameSavedRouteRequestDto,
    ): NetworkResult<SavedRouteSummaryDto>

    /** `DELETE /me/saved-routes/{savedRouteId}` (`deleteSavedRoute`). */
    @DELETE("me/saved-routes/{savedRouteId}")
    suspend fun deleteSavedRoute(
        @Path("savedRouteId") savedRouteId: String,
    ): NetworkResult<Unit>

    /** `DELETE /me` (`deleteAccount`). */
    @DELETE("me")
    suspend fun deleteAccount(): NetworkResult<Unit>

    /** `POST /me/drives/claim` (`claimDrives`). */
    @POST("me/drives/claim")
    suspend fun claimDrives(
        @Body request: ClaimDrivesRequestDto,
    ): NetworkResult<Unit>
}
