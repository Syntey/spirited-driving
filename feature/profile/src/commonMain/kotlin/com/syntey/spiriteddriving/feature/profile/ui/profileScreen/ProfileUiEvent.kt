package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the profile screen. */
sealed interface ProfileUiEvent : UiEvent {

    /** Open [url] in the browser. */
    data class OpenWebPage(val url: String) : ProfileUiEvent
}
