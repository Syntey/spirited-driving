package com.syntey.spiriteddriving.data.network.dto.savedroute

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `POST /me/saved-routes` (`SaveRouteRequest`). */
@Serializable
internal data class SaveRouteRequestDto(@SerialName("routeId") val routeId: String, @SerialName("name") val name: String? = null)
