package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRater
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class ApiDriveRater(private val api: SpiritedDrivingApi) : DriveRater {

    override suspend fun rate(driveId: String, rating: Rating) {
        val result = api.rateDrive(driveId = driveId, request = rating.toDto())
        // Matched by status, as ApiDriveRecorder does: the backend's 403 and 404 may come without a Problem code.
        // A drive that isn't this user's or isn't there can't take a rating, however often it's sent.
        val error = (result as? NetworkResult.Failure)?.error
        if (error is NetworkError.HttpError && error.statusCode in NOT_RATABLE_STATUSES) {
            throw DriveNotRatableException(error)
        }
        result.getOrThrowDomainError()
    }

    private companion object {
        val NOT_RATABLE_STATUSES = setOf(HttpStatusCode.Forbidden.value, HttpStatusCode.NotFound.value)
    }
}
