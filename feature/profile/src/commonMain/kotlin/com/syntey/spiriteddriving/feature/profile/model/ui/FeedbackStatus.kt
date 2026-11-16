package com.syntey.spiriteddriving.feature.profile.model.ui

/** Where sending the feedback stands. Offline and Failed keep the text, so the user can send it again. */
enum class FeedbackStatus {
    Editing,
    Sending,
    Sent,
    Offline,
    Failed,
}
