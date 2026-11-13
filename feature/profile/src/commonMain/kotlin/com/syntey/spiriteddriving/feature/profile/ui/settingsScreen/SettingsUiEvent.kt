package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the settings screen. */
sealed interface SettingsUiEvent : UiEvent {

    /** Open [url] in the browser. */
    data class OpenWebPage(val url: String) : SettingsUiEvent
}
