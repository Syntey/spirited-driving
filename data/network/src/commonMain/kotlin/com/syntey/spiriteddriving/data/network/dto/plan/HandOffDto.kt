package com.syntey.spiriteddriving.data.network.dto.plan

import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A route's hand-off to the maps app (`HandOff` schema). */
@Serializable
internal data class HandOffDto(
    @SerialName("waypoints") val waypoints: List<GeoPointDto>,
    @SerialName("followedShare") val followedShare: Double,
)
