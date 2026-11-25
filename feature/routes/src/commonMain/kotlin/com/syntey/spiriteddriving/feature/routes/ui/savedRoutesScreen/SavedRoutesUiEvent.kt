package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the saved routes screen. */
sealed interface SavedRoutesUiEvent : UiEvent {

    /** A change needs a connection, and there's none. */
    data object ChangesOffline : SavedRoutesUiEvent

    /** The route couldn't be renamed for another reason. */
    data object RenameFailed : SavedRoutesUiEvent

    /** The route couldn't be deleted for another reason. */
    data object DeleteFailed : SavedRoutesUiEvent
}
