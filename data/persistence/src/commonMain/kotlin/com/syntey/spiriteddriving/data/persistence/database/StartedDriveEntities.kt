package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * One row per started drive. [position] grows with each add, so the highest is the most recently added, and [rating] is
 * `StoredRating` JSON. The shape is the stored format, so renaming a column loses the kept drives.
 */
@Entity(tableName = "started_drive")
internal data class StartedDriveEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val routeId: String,
    val startedAtEpochMillis: Long,
    val plannedDurationSeconds: Long,
    val isRecorded: Boolean,
    val rating: String?,
    val isRatingSent: Boolean,
    val isNotDriven: Boolean,
)

internal fun Drive.toEntity(position: Int, rating: String?) = StartedDriveEntity(
    id = id,
    position = position,
    routeId = routeId,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    plannedDurationSeconds = plannedDuration.inWholeSeconds,
    isRecorded = isRecorded,
    rating = rating,
    isRatingSent = isRatingSent,
    isNotDriven = isNotDriven,
)

internal fun StartedDriveEntity.toDomain(rating: Rating?) = Drive(
    id = id,
    routeId = routeId,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    plannedDuration = plannedDurationSeconds.seconds,
    isRecorded = isRecorded,
    rating = rating,
    isRatingSent = isRatingSent,
    isNotDriven = isNotDriven,
)
