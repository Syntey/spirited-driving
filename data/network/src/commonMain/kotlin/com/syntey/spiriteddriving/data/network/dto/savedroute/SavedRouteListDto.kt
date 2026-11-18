package com.syntey.spiriteddriving.data.network.dto.savedroute

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /me/saved-routes` (`SavedRouteList`). */
@Serializable
internal data class SavedRouteListDto(@SerialName("items") val items: List<SavedRouteSummaryDto>)
