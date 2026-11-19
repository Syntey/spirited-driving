package com.syntey.spiriteddriving.data.persistence.persistence.savedroute

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.HandOff
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.geo.toStored
import com.syntey.spiriteddriving.data.persistence.persistence.place.StoredPlace
import com.syntey.spiriteddriving.data.persistence.persistence.place.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.place.toStored
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

/**
 * A full route as kept on the device, with its line and hand-off. Its shape is the stored format, so renaming a field
 * loses the kept routes.
 */
@Serializable
internal data class StoredRoute(
    val id: String,
    val kind: String,
    val start: StoredPlace,
    val destination: StoredPlace,
    val durationSeconds: Long,
    val distanceMetres: Long,
    val extraDurationSeconds: Long,
    val extraDistanceMetres: Long,
    val funLevel: Int,
    val curvyDistanceMetres: Long,
    val ascentMetres: Long,
    val line: List<StoredPoint>,
    val handOffWaypoints: List<StoredPoint>,
    val followedShare: Double,
    val attribution: String,
)

internal fun Route.toStored() = StoredRoute(
    id = id,
    kind = when (kind) {
        RouteKind.Fastest -> "FASTEST"
        RouteKind.Fun -> "FUN"
    },
    start = start.toStored(),
    destination = destination.toStored(),
    durationSeconds = duration.inWholeSeconds,
    distanceMetres = distance.inWholeMetres.toLong(),
    extraDurationSeconds = extraDuration.inWholeSeconds,
    extraDistanceMetres = extraDistance.inWholeMetres.toLong(),
    funLevel = funLevel,
    curvyDistanceMetres = stats.curvyDistance.inWholeMetres.toLong(),
    ascentMetres = stats.ascent.inWholeMetres.toLong(),
    line = line.map { it.toStored() },
    handOffWaypoints = handOff.waypoints.map { it.toStored() },
    followedShare = handOff.followedShare,
    attribution = attribution,
)

/** An unknown kind reads as [RouteKind.Fun], the kind of every route but the fastest. */
internal fun StoredRoute.toDomain() = Route(
    id = id,
    kind = if (kind == "FASTEST") RouteKind.Fastest else RouteKind.Fun,
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMetres.toInt().metres,
    extraDuration = extraDurationSeconds.seconds,
    extraDistance = extraDistanceMetres.toInt().metres,
    funLevel = funLevel,
    stats = RouteStats(
        curvyDistance = curvyDistanceMetres.toInt().metres,
        ascent = ascentMetres.toInt().metres,
    ),
    line = line.map { it.toDomain() },
    handOff = HandOff(
        waypoints = handOffWaypoints.map { it.toDomain() },
        followedShare = followedShare,
    ),
    attribution = attribution,
)
