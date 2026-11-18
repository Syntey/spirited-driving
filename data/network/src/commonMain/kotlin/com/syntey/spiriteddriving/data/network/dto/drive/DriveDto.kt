package com.syntey.spiriteddriving.data.network.dto.drive

import com.syntey.spiriteddriving.data.network.dto.plan.RouteDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /drives/{driveId}` (`Drive`), with the fields the app uses. */
@Serializable
internal data class DriveDto(
    @SerialName("id") val id: String,
    @SerialName("startedAt") val startedAt: String,
    @SerialName("route") val route: RouteDto,
    @SerialName("rating") val rating: RatingDto? = null,
)
