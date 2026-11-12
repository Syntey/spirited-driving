package com.syntey.spiriteddriving.data.network.dto.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GeoJSON `MultiPolygon` (`MultiPolygon` schema). Each position is `[longitude, latitude]`. `type` is always
 * `MultiPolygon`, so it isn't modeled.
 */
@Serializable
internal data class MultiPolygonDto(@SerialName("coordinates") val coordinates: List<List<List<List<Double>>>>)
