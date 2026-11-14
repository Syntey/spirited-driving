package com.syntey.spiriteddriving.data.network.dto.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `PUT /drives/{driveId}` (`RecordDriveRequest`). */
@Serializable
internal data class RecordDriveRequestDto(@SerialName("routeId") val routeId: String, @SerialName("startedAt") val startedAt: String)
