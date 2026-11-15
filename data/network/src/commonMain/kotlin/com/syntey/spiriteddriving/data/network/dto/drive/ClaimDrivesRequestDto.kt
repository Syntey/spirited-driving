package com.syntey.spiriteddriving.data.network.dto.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `POST /me/drives/claim` (`ClaimDrivesRequest`). */
@Serializable
internal data class ClaimDrivesRequestDto(@SerialName("driveIds") val driveIds: List<String>)
