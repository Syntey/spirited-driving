package com.syntey.spiriteddriving.data.persistence.persistence.drive

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toStored
import com.syntey.spiriteddriving.data.persistence.persistence.place.StoredPlace
import com.syntey.spiriteddriving.data.persistence.persistence.place.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.place.toStored
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * A drive question as kept on the device. Its shape is the stored format, so renaming a field loses the kept question.
 */
@Serializable
internal data class StoredDriveQuestion(
    @SerialName("driveId") val driveId: String,
    @SerialName("startedAtEpochMillis") val startedAtEpochMillis: Long,
    @SerialName("routeNumber") val routeNumber: Int,
    @SerialName("start") val start: StoredPoint,
    @SerialName("destination") val destination: StoredPlace,
    @SerialName("durationSeconds") val durationSeconds: Long,
    @SerialName("distanceMetres") val distanceMetres: Double,
    @SerialName("line") val line: List<StoredPoint>,
    @SerialName("attribution") val attribution: String,
)

internal fun DriveQuestion.toStored() = StoredDriveQuestion(
    driveId = driveId,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    routeNumber = routeNumber,
    start = start.toStored(),
    destination = destination.toStored(),
    durationSeconds = duration.inWholeSeconds,
    distanceMetres = distance.inMetres,
    line = line.map { it.toStored() },
    attribution = attribution,
)

internal fun StoredDriveQuestion.toDomain() = DriveQuestion(
    driveId = driveId,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    routeNumber = routeNumber,
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMetres.metres,
    line = line.map { it.toDomain() },
    attribution = attribution,
)
