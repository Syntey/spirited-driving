package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the trip plan screen. */
sealed interface TripPlanUiEvent : UiEvent {

    /** Tapped a place outside the covered area: the sheet explains it isn't covered yet. */
    data object NotCovered : TripPlanUiEvent

    /** Saving a route needs a connection. */
    data object SaveRouteOffline : TripPlanUiEvent

    /** Saving a route failed for another reason. */
    data object SaveRouteFailed : TripPlanUiEvent

    /** Removing a saved route needs a connection. */
    data object RemoveRouteOffline : TripPlanUiEvent

    /** Removing a saved route failed for another reason. */
    data object RemoveRouteFailed : TripPlanUiEvent
}
