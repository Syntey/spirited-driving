package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the trip plan screen. */
sealed interface TripPlanUiEvent : UiEvent {

    /** Tapped a place outside the covered area: the sheet explains it isn't covered yet. */
    data object NotCovered : TripPlanUiEvent
}
