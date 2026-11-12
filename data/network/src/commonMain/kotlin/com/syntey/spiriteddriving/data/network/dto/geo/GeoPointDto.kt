package com.syntey.spiriteddriving.data.network.dto.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A point (`GeoPoint` schema), in WGS 84 degrees. */
@Serializable
internal data class GeoPointDto(@SerialName("lat") val lat: Double, @SerialName("lon") val lon: Double)
