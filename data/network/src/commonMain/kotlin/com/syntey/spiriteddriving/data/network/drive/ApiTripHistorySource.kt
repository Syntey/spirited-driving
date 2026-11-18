package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.PastDriveNotFoundException
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.TripHistorySource
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.plan.toDomain
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class ApiTripHistorySource(private val api: SpiritedDrivingApi) : TripHistorySource {

    override suspend fun list(cursor: String?, limit: Int): PastDrivePage =
        api.listDrives(limit = limit, cursor = cursor).getOrThrowDomainError().toDomain()

    override suspend fun getRoute(driveId: String, maxHandOffWaypoints: Int): Route {
        val result = api.getDrive(driveId = driveId, maxHandOffWaypoints = maxHandOffWaypoints)
        result.throwIfNotFound()
        return result.getOrThrowDomainError().route.toDomain()
    }

    override suspend fun clear() {
        api.clearDrives().getOrThrowDomainError()
    }

    // Matched by status: the backend's 404 may come without a Problem code, which getOrThrowDomainError needs.
    // A 403 is another user's drive, which is gone for this user too.
    private fun NetworkResult<*>.throwIfNotFound() {
        val error = (this as? NetworkResult.Failure)?.error
        if (error is NetworkError.HttpError && error.statusCode in NOT_FOUND_STATUSES) {
            throw PastDriveNotFoundException(error)
        }
    }

    private companion object {
        val NOT_FOUND_STATUSES = setOf(HttpStatusCode.NotFound.value, HttpStatusCode.Forbidden.value)
    }
}
