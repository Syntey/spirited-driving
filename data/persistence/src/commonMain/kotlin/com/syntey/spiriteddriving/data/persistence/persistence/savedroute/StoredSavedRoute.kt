package com.syntey.spiriteddriving.data.persistence.persistence.savedroute

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.RouteSummary
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.data.persistence.persistence.place.StoredPlace
import com.syntey.spiriteddriving.data.persistence.persistence.place.toDomain
import com.syntey.spiriteddriving.data.persistence.persistence.place.toStored
import com.syntey.spiriteddriving.data.persistence.persistence.route.ascentMetres
import com.syntey.spiriteddriving.data.persistence.persistence.route.curvyDistanceMetres
import com.syntey.spiriteddriving.data.persistence.persistence.route.routeStatsOf
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
    id = id.value,
    name = name,
    savedAtEpochMillis = savedAt.toEpochMilliseconds(),
    routeId = route.id,
    start = route.start.toStored(),
    destination = route.destination.toStored(),
    durationSeconds = route.duration.inWholeSeconds,
    distanceMetres = route.distance.inWholeMetres.toLong(),
    funLevel = route.funLevel,
    curvyDistanceMetres = route.stats.curvyDistanceMetres,
    ascentMetres = route.stats.ascentMetres,
)

internal fun StoredSavedRoute.toDomain() = SavedRoute(
    id = SavedRouteId(id),
    name = name,
    savedAt = Instant.fromEpochMilliseconds(savedAtEpochMillis),
    route = RouteSummary(
        id = routeId,
        start = start.toDomain(),
        destination = destination.toDomain(),
        duration = durationSeconds.seconds,
        distance = distanceMetres.toInt().metres,
        funLevel = funLevel,
        stats = routeStatsOf(curvyDistanceMetres, ascentMetres),
    ),
)
