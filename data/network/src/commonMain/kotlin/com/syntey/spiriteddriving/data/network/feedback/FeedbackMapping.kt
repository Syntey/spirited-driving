package com.syntey.spiriteddriving.data.network.feedback

import com.syntey.spiriteddriving.core.domain.feedback.Feedback
import com.syntey.spiriteddriving.data.network.dto.feedback.FeedbackRequestDto

internal fun Feedback.toDto(): FeedbackRequestDto = FeedbackRequestDto(
    message = message,
    appVersion = appVersion,
    appBuild = appBuild,
    os = os,
    device = device,
)
