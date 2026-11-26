package com.syntey.spiriteddriving.data.persistence.database

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place

/** A [Place] spread over columns, with its point as latitude and longitude. */
internal data class PlaceColumns(val name: String?, val area: String?, val latitude: Double, val longitude: Double)

internal fun Place.toColumns() = PlaceColumns(
    name = name,
    area = area,
    latitude = location.latitude,
    longitude = location.longitude,
)

internal fun PlaceColumns.toDomain() = Place(
    name = name,
    area = area,
    location = GeoPoint(latitude = latitude, longitude = longitude),
)
