package com.syntey.spiriteddriving.data.network.dto.savedroute

import com.syntey.spiriteddriving.data.network.dto.plan.RouteDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /me/saved-routes/{savedRouteId}` (`SavedRoute` schema), with its full route. */
@Serializable
internal data class SavedRouteDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("savedAt") val savedAt: String,
    @SerialName("route") val route: RouteDto,
)
