package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the account screen. */
sealed interface AccountUiEvent : UiEvent {

    /** Deleting the account failed; the user stays on the screen and can try again. */
    data object DeleteFailed : AccountUiEvent
}
