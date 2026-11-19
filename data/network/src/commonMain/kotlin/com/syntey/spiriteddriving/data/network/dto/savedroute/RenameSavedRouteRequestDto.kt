package com.syntey.spiriteddriving.data.network.dto.savedroute

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `PATCH /me/saved-routes/{savedRouteId}` (`RenameSavedRouteRequest`). */
@Serializable
internal data class RenameSavedRouteRequestDto(@SerialName("name") val name: String)
