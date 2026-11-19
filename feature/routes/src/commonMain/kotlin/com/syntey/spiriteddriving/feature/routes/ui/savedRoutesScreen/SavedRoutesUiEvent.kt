package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import app.futured.arkitekt.decompose.presentation.UiEvent
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId

/** One-time events from the saved routes screen. */
sealed interface SavedRoutesUiEvent : UiEvent {

    /** A route was removed from the list; the screen offers to undo it for a few seconds. */
    data class RouteDeleted(val savedRouteId: SavedRouteId) : SavedRoutesUiEvent

    /** A change needs a connection, and there's none. */
    data object ChangesOffline : SavedRoutesUiEvent

    /** The route couldn't be renamed for another reason. */
    data object RenameFailed : SavedRoutesUiEvent

    /** The route couldn't be deleted for another reason. */
    data object DeleteFailed : SavedRoutesUiEvent
}
