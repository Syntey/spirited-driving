package com.syntey.spiriteddriving.data.network.dto.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A drive's rating (`Rating` schema), with the fields the app uses. */
@Serializable
internal data class RatingDto(
    @SerialName("thumb") val thumb: String,
    @SerialName("reasons") val reasons: List<String>,
    @SerialName("ratedAt") val ratedAt: String,
)
