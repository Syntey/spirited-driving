package com.syntey.spiriteddriving.data.network.dto.savedroute

import com.syntey.spiriteddriving.data.network.dto.plan.RouteSummaryDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A saved route without its line and hand-off (`SavedRouteSummary` schema). */
@Serializable
internal data class SavedRouteSummaryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("savedAt") val savedAt: String,
    @SerialName("route") val route: RouteSummaryDto,
)
