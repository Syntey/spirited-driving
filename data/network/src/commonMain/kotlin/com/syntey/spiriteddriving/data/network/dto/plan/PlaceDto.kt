package com.syntey.spiriteddriving.data.network.dto.plan

import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A place of a route (`Place` schema). */
@Serializable
internal data class PlaceDto(
    @SerialName("location") val location: GeoPointDto,
    @SerialName("name") val name: String,
    @SerialName("area") val area: String? = null,
)
