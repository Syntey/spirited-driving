package com.syntey.spiriteddriving.data.persistence.persistence.drive

import com.syntey.spiriteddriving.core.domain.drive.Drive
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * A drive as kept on the device. Its shape is the stored format, so renaming a field loses the kept drives.
 */
@Serializable
internal data class StoredDrive(
    val id: String,
    val routeId: String,
    val startedAtEpochMillis: Long,
    val plannedDurationSeconds: Long,
    val isRecorded: Boolean,
)

internal fun Drive.toStored() = StoredDrive(
    id = id,
    routeId = routeId,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    plannedDurationSeconds = plannedDuration.inWholeSeconds,
    isRecorded = isRecorded,
)

internal fun StoredDrive.toDomain() = Drive(
    id = id,
    routeId = routeId,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    plannedDuration = plannedDurationSeconds.seconds,
    isRecorded = isRecorded,
)
