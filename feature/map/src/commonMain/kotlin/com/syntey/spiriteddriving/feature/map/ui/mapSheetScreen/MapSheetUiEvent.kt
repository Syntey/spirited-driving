package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.decompose.presentation.UiEvent

/** One-time events from the map sheet screen. */
sealed interface MapSheetUiEvent : UiEvent {

    /** Tapped a place outside the covered area: the sheet explains it isn't covered yet. */
    data object NotCovered : MapSheetUiEvent

    /** Saving a route needs a connection. */
    data object SaveRouteOffline : MapSheetUiEvent

    /** Saving a route failed for another reason. */
    data object SaveRouteFailed : MapSheetUiEvent

    /** Removing a saved route needs a connection. */
    data object RemoveRouteOffline : MapSheetUiEvent

    /** Removing a saved route failed for another reason. */
    data object RemoveRouteFailed : MapSheetUiEvent
}
