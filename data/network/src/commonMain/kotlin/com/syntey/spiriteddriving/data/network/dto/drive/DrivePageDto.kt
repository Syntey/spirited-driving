package com.syntey.spiriteddriving.data.network.dto.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /me/drives` (`DrivePage`). */
@Serializable
internal data class DrivePageDto(
    @SerialName("items") val items: List<DriveSummaryDto>,
    @SerialName("nextCursor") val nextCursor: String? = null,
)
