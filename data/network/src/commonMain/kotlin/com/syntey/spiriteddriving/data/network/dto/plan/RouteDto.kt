package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A route of a plan (`Route` schema), with the fields the app uses. */
@Serializable
internal data class RouteDto(
    @SerialName("id") val id: String,
    @SerialName("kind") val kind: String,
    @SerialName("start") val start: PlaceDto,
    @SerialName("destination") val destination: PlaceDto,
    @SerialName("durationSeconds") val durationSeconds: Int,
    @SerialName("distanceMeters") val distanceMeters: Int,
    @SerialName("extraDurationSeconds") val extraDurationSeconds: Int,
    @SerialName("extraDistanceMeters") val extraDistanceMeters: Int,
    @SerialName("funLevel") val funLevel: Int,
    @SerialName("stats") val stats: RouteStatsDto,
    @SerialName("polyline") val polyline: String,
    @SerialName("handOff") val handOff: HandOffDto,
    @SerialName("attribution") val attribution: String,
)
