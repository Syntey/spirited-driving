package com.syntey.spiriteddriving.data.network.dto.place

import com.syntey.spiriteddriving.data.network.dto.geo.GeoPointDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An item of `PlaceSearchResult` (`PlaceSuggestion` schema). */
@Serializable
internal data class PlaceSuggestionDto(
    @SerialName("location") val location: GeoPointDto,
    @SerialName("name") val name: String,
    @SerialName("area") val area: String? = null,
    @SerialName("covered") val covered: Boolean,
    @SerialName("kind") val kind: String,
)
