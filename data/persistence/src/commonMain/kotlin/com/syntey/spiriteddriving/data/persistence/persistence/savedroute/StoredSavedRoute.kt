package com.syntey.spiriteddriving.data.persistence.persistence.savedroute

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.RouteStats
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.data.persistence.persistence.place.StoredPlace
import com.syntey.spiriteddriving.data.persistence.persistence.place.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.place.toStored
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * A saved route as kept on the device, for the list. Its shape is the stored format, so renaming a field loses the kept
 * saved routes.
 */
@Serializable
internal data class StoredSavedRoute(
    val id: String,
    val name: String,
    val savedAtEpochMillis: Long,
    val routeId: String,
    val start: StoredPlace,
    val destination: StoredPlace,
    val durationSeconds: Long,
    val distanceMetres: Long,
    val funLevel: Int,
    val curvyDistanceMetres: Long,
    val ascentMetres: Long,
)

internal fun SavedRoute.toStored() = StoredSavedRoute(
    id = id,
    name = name,
    savedAtEpochMillis = savedAt.toEpochMilliseconds(),
    routeId = routeId,
    start = start.toStored(),
    destination = destination.toStored(),
    durationSeconds = duration.inWholeSeconds,
    distanceMetres = distance.inWholeMetres.toLong(),
    funLevel = funLevel,
    curvyDistanceMetres = stats.curvyDistance.inWholeMetres.toLong(),
    ascentMetres = stats.ascent.inWholeMetres.toLong(),
)

internal fun StoredSavedRoute.toDomain() = SavedRoute(
    id = id,
    name = name,
    savedAt = Instant.fromEpochMilliseconds(savedAtEpochMillis),
    routeId = routeId,
    start = start.toDomain(),
    destination = destination.toDomain(),
    duration = durationSeconds.seconds,
    distance = distanceMetres.toInt().metres,
    funLevel = funLevel,
    stats = RouteStats(
        curvyDistance = curvyDistanceMetres.toInt().metres,
        ascent = ascentMetres.toInt().metres,
    ),
)
