package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class ApiDriveRecorder(private val api: SpiritedDrivingApi) : DriveRecorder {

    override suspend fun record(drive: Drive) {
        val result = api.recordDrive(driveId = drive.id, request = drive.toDto())
        // Matched by status: the backend's 404 and 409 may come without a Problem code, which getOrThrowDomainError needs
        val error = (result as? NetworkResult.Failure)?.error
        if (error is NetworkError.HttpError && error.statusCode in NOT_RECORDABLE_STATUSES) {
            throw DriveNotRecordableException(error)
        }
        result.getOrThrowDomainError()
    }

    private companion object {
        val NOT_RECORDABLE_STATUSES = setOf(HttpStatusCode.NotFound.value, HttpStatusCode.Conflict.value)
    }
}
