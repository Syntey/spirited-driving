package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

internal fun boundsOf(points: List<GeoPoint>): GeoBounds =
    points.drop(1).fold(GeoBounds(points.first(), points.first())) { bounds, point -> bounds.including(point) }

// Grows around the centre, so a short or narrow area is framed with the streets around it
internal fun GeoBounds.withMinimumSpan(): GeoBounds {
    val latSpan = northEast.latitude - southWest.latitude
    val lonSpan = northEast.longitude - southWest.longitude
    if (latSpan >= Constants.Map.MIN_ROUTE_LAT_SPAN && lonSpan >= Constants.Map.MIN_ROUTE_LON_SPAN) return this
    val centreLat = (northEast.latitude + southWest.latitude) / 2
    val centreLon = (northEast.longitude + southWest.longitude) / 2
    val halfLat = maxOf(latSpan, Constants.Map.MIN_ROUTE_LAT_SPAN) / 2
    val halfLon = maxOf(lonSpan, Constants.Map.MIN_ROUTE_LON_SPAN) / 2
    return GeoBounds(
        southWest = GeoPoint(latitude = centreLat - halfLat, longitude = centreLon - halfLon),
        northEast = GeoPoint(latitude = centreLat + halfLat, longitude = centreLon + halfLon),
    )
}
