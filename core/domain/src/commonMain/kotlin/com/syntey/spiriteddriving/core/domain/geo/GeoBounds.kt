package com.syntey.spiriteddriving.core.domain.geo

/** An area between two corners, in WGS 84 degrees. */
data class GeoBounds(val southWest: GeoPoint, val northEast: GeoPoint)
