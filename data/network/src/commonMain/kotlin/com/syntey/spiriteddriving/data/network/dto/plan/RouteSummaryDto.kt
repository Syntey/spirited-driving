package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A route without its line and hand-off, for lists (`RouteSummary` schema), with the fields the app uses. */
@Serializable
internal data class RouteSummaryDto(
    @SerialName("id") val id: String,
    @SerialName("kind") val kind: String,
    @SerialName("start") val start: PlaceDto,
    @SerialName("destination") val destination: PlaceDto,
    @SerialName("durationSeconds") val durationSeconds: Int,
    @SerialName("distanceMeters") val distanceMeters: Int,
    @SerialName("funLevel") val funLevel: Int,
    @SerialName("stats") val stats: RouteStatsDto,
)
