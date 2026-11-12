package com.syntey.spiriteddriving.core.domain.geo

/** An area between two corners, in WGS 84 degrees. */
data class GeoBounds(val southWest: GeoPoint, val northEast: GeoPoint) {

    /** These bounds, grown just enough to include [point]. */
    fun including(point: GeoPoint): GeoBounds = GeoBounds(
        southWest = GeoPoint(
            latitude = minOf(southWest.latitude, point.latitude),
            longitude = minOf(southWest.longitude, point.longitude),
        ),
        northEast = GeoPoint(
            latitude = maxOf(northEast.latitude, point.latitude),
            longitude = maxOf(northEast.longitude, point.longitude),
        ),
    )
}
