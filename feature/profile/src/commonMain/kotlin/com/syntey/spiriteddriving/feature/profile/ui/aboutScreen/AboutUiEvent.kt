package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the about screen. */
sealed interface AboutUiEvent : UiEvent {

    /** Open [url] in the browser. */
    data class OpenWebPage(val url: String) : AboutUiEvent
}
