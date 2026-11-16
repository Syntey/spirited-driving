package com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen

import com.syntey.spiriteddriving.feature.profile.model.ui.FeedbackStatus
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class FeedbackViewState(
    val message: String = "",
    val status: FeedbackStatus = FeedbackStatus.Editing,
    /** What is sent along with the message, such as "Sent with: Spirited Driving 1.0.0 (42) · iOS 26.1 · iPhone 17 Pro". */
    val sentWith: StringDesc = "".desc(),
) {
    val canSend: Boolean get() = message.isNotBlank() && status != FeedbackStatus.Sending

    val isEditable: Boolean get() = status != FeedbackStatus.Sending
}
