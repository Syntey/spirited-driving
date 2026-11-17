package com.syntey.spiriteddriving.data.network.dto.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `PUT /drives/{driveId}/rating` (`RatingInput`). */
@Serializable
internal data class RatingInputDto(@SerialName("thumb") val thumb: String, @SerialName("reasons") val reasons: List<String>)
