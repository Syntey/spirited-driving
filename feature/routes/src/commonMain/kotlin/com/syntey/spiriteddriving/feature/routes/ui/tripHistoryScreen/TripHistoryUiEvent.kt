package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the trip history screen. */
sealed interface TripHistoryUiEvent : UiEvent {

    /** A change needs a connection, and there's none. */
    data object ChangesOffline : TripHistoryUiEvent

    /** The drive couldn't be deleted for another reason. */
    data object DeleteFailed : TripHistoryUiEvent

    /** The history couldn't be cleared for another reason. */
    data object ClearFailed : TripHistoryUiEvent
}
