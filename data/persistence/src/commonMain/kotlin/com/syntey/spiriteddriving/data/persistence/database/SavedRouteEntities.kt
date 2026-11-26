package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.RouteSummary
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.data.persistence.persistence.route.ascentMetres
import com.syntey.spiriteddriving.data.persistence.persistence.route.curvyDistanceMetres
import com.syntey.spiriteddriving.data.persistence.persistence.route.routeStatsOf
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * One row per saved route. [position] keeps the list order, and the shape is the stored format, so renaming a column
 * loses the kept saved routes.
 */
@Entity(tableName = "saved_route")
internal data class SavedRouteEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val name: String,
    val savedAtEpochMillis: Long,
    val routeId: String,
    @Embedded(prefix = "start_") val start: PlaceColumns,
    @Embedded(prefix = "destination_") val destination: PlaceColumns,
    val durationSeconds: Long,
    val distanceMetres: Long,
    val funLevel: Int,
    val curvyDistanceMetres: Long,
    val ascentMetres: Long,
)

/** The full route of a saved route, as its [route] JSON. No foreign key to the list: the DAO removes it with its saved route. */
@Entity(tableName = "saved_route_detail")
internal data class SavedRouteDetailEntity(@PrimaryKey val savedRouteId: String, val route: String)

/**
 * The single row that records a kept list. Its existence means a list is kept, so a kept empty list differs from
 * nothing kept.
 */
@Entity(tableName = "saved_routes_meta")
internal data class SavedRoutesMetaEntity(@PrimaryKey val id: Int = ROW_ID) {
    companion object {
        const val ROW_ID = 1
    }
}

/** A [Place] spread over columns, with its point as latitude and longitude. */
internal data class PlaceColumns(val name: String?, val area: String?, val latitude: Double, val longitude: Double)

internal fun SavedRoute.toEntity(position: Int) = SavedRouteEntity(
    id = id.value,
    position = position,
    name = name,
    savedAtEpochMillis = savedAt.toEpochMilliseconds(),
    routeId = route.id,
    start = route.start.toColumns(),
    destination = route.destination.toColumns(),
    durationSeconds = route.duration.inWholeSeconds,
    distanceMetres = route.distance.inWholeMetres.toLong(),
    funLevel = route.funLevel,
    curvyDistanceMetres = route.stats.curvyDistanceMetres,
    ascentMetres = route.stats.ascentMetres,
)

internal fun SavedRouteEntity.toDomain() = SavedRoute(
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

private fun Place.toColumns() = PlaceColumns(
    name = name,
    area = area,
    latitude = location.latitude,
    longitude = location.longitude,
)

private fun PlaceColumns.toDomain() = Place(
    name = name,
    area = area,
    location = GeoPoint(latitude = latitude, longitude = longitude),
)
