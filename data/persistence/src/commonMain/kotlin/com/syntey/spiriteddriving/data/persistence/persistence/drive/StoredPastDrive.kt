package com.syntey.spiriteddriving.data.persistence.persistence.drive

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.data.persistence.persistence.place.StoredPlace
import com.syntey.spiriteddriving.data.persistence.persistence.place.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.place.toStored
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * A trip history drive as kept on the device. Its shape is the stored format, so renaming a field loses the kept trip
 * history.
 */
@Serializable
internal data class StoredPastDrive(
    val id: String,
    val startedAtEpochMillis: Long,
    val routeId: String,
    val kind: String,
    val start: StoredPlace,
    val destination: StoredPlace,
    val durationSeconds: Long,
    val distanceMetres: Long,
    val funLevel: Int,
    val rating: StoredRating? = null,
)

internal fun PastDrive.toStored() = StoredPastDrive(
    id = id,
    startedAtEpochMillis = startedAt.toEpochMilliseconds(),
    routeId = routeId,
    kind = when (kind) {
        RouteKind.Fastest -> "fastest"
        RouteKind.Fun -> "fun"
    },
    start = start.toStored(),
    destination = destination.toStored(),
    durationSeconds = duration.inWholeSeconds,
    distanceMetres = distance.inWholeMetres.toLong(),
    funLevel = funLevel,
    rating = rating?.toStored(),
)

/** An unknown kind reads as [RouteKind.Fun], the kind of every route but the fastest. */
internal fun StoredPastDrive.toDomain() = PastDrive(
    id = id,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMillis),
    routeId = routeId,
    kind = if (kind == "fastest") RouteKind.Fastest else RouteKind.Fun,
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMetres.toInt().metres,
    funLevel = funLevel,
    rating = rating?.toDomain(),
)
