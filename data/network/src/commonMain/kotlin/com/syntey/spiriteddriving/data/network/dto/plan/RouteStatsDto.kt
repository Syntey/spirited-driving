package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A route's road facts (`RouteStats` schema). */
@Serializable
internal data class RouteStatsDto(
    @SerialName("curvyDistanceMeters") val curvyDistanceMeters: Int,
    @SerialName("ascentMeters") val ascentMeters: Int,
)
