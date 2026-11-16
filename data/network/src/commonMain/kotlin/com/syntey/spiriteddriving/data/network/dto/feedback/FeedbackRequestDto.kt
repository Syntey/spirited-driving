package com.syntey.spiriteddriving.data.network.dto.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `POST /feedback` (`FeedbackRequest`). */
@Serializable
internal data class FeedbackRequestDto(@SerialName("message") val message: String)
