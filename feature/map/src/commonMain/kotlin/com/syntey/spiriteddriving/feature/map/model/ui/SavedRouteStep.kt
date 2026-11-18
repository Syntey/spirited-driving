package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute

/**
 * The one-route step of the sheet: a saved route shown on its own, ready to drive.
 *
 * @property savedRoute The saved route, as the list kept it. Saving the route again gives it a new saved route, which replaces this.
 * @property route The full route; null until it's loaded.
 * @property status Where loading the full route is.
 * @property isHandedOff Whether the route was handed off to the maps app, so the rating step may take its place when the user comes back.
 */
data class SavedRouteStep(
    val savedRoute: SavedRoute,
    val route: Route? = null,
    val status: SavedRouteStatus = SavedRouteStatus.Loading,
    val isHandedOff: Boolean = false,
)

/** Where loading a saved route's full route is. */
enum class SavedRouteStatus {
    /** Waiting for the route. */
    Loading,

    /** The route is loaded. */
    Ready,

    /** The route isn't on the device and there's no connection. */
    Offline,

    /** Loading failed for another reason. */
    Failed,
}
