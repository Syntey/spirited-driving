package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackViewState
import dev.icerock.moko.resources.desc.desc

internal object FeedbackPreviewStates {
    private const val SENT_WITH = "Sent with: Spirited Driving 1.0.0 (42) · iOS 26.1 · iPhone 17 Pro"
    const val SAMPLE_MESSAGE = "The lane closure near the lake was missing from the route. Otherwise the twisty bit was great."

    fun viewState(message: String = "", status: FeedbackStatus = FeedbackStatus.Editing) = FeedbackViewState(
        message = message,
        status = status,
        sentWith = SENT_WITH.desc(),
    )
}
