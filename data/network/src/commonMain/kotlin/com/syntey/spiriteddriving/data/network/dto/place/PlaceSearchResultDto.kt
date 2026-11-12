package com.syntey.spiriteddriving.data.network.dto.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /places/search` (`PlaceSearchResult` schema). */
@Serializable
internal data class PlaceSearchResultDto(@SerialName("items") val items: List<PlaceSuggestionDto>)
