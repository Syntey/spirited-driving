package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * One row per kept Trip history drive. [position] keeps the newest-first order, and [rating] is the shown rating as
 * `StoredRating` JSON. The shape is the stored format, so renaming a column loses the kept Trip history.
 */
@Entity(tableName = "past_drive")
internal data class PastDriveEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val startedAtEpochMillis: Long,
    val routeId: String,
    val kind: String,
    @Embedded(prefix = "start_") val start: PlaceColumns,
    @Embedded(prefix = "destination_") val destination: PlaceColumns,
    val durationSeconds: Long,
    val distanceMetres: Long,
    val funLevel: Int,
    val rating: String?,
)

/**
 * The single row whose existence means a Trip history is kept, with the cursor of the page after the kept drives.
 */
@Entity(tableName = "trip_history_meta")
internal data class TripHistoryMetaEntity(@PrimaryKey val id: Int = ROW_ID, val nextCursor: String?) {
    companion object {
        const val ROW_ID = 1
    }
}

/**
 * The full route of a drive as `StoredRoute` JSON. No foreign key to `past_drive`: a drive's route is kept from its
 * start, before the drive is in the kept list.
 */
@Entity(tableName = "past_drive_route")
internal data class PastDriveRouteEntity(@PrimaryKey val driveId: String, val route: String)

/**
 * A rating not sent yet, as `StoredRating` JSON. Its own table so that it outlives a replaced page without its drive
 * and is applied again when the drive is appended.
 */
@Entity(tableName = "unsent_rating")
internal data class UnsentRatingEntity(@PrimaryKey val driveId: String, val rating: String)

/** The kept drives in order and the cursor of the page after them. */
internal data class KeptTripHistory(val drives: List<PastDriveEntity>, val nextCursor: String?)

internal fun PastDrive.toEntity(position: Int, rating: String?) = PastDriveEntity(
    id = id,
    position = position,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    routeId = routeId,
    kind = when (kind) {
        RouteKind.Fastest -> "fastest"
        RouteKind.Fun -> "fun"
    },
    start = start.toColumns(),
    destination = destination.toColumns(),
    durationSeconds = duration.inWholeSeconds,
    distanceMetres = distance.inWholeMetres.toLong(),
    funLevel = funLevel,
    rating = rating,
)

/** An unknown kind reads as [RouteKind.Fun], the kind of every route but the fastest. */
internal fun PastDriveEntity.toDomain(rating: Rating?) = PastDrive(
    id = id,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    routeId = routeId,
    kind = if (kind == "fastest") RouteKind.Fastest else RouteKind.Fun,
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMetres.toInt().metres,
    funLevel = funLevel,
    rating = rating,
)
