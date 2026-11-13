package com.syntey.spiriteddriving.data.network.dto.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `POST /feedback` (`FeedbackRequest`). */
@Serializable
internal data class FeedbackRequestDto(
    @SerialName("message") val message: String,
    @SerialName("appVersion") val appVersion: String,
    @SerialName("appBuild") val appBuild: String,
    @SerialName("os") val os: String,
    @SerialName("device") val device: String,
)
