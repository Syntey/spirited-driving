package com.syntey.spiriteddriving.data.network.dto.drive

import com.syntey.spiriteddriving.data.network.dto.plan.RouteSummaryDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A drive of a trip history page (`DriveSummary` schema), with the fields the app uses. */
@Serializable
internal data class DriveSummaryDto(
    @SerialName("id") val id: String,
    @SerialName("startedAt") val startedAt: String,
    @SerialName("route") val route: RouteSummaryDto,
    @SerialName("rating") val rating: RatingDto? = null,
)
