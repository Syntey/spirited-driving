package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute

/**
 * The one-route step of the sheet: a route shown on its own, ready to drive, from Saved routes or Trip history.
 *
 * @property origin Where the route comes from.
 * @property route The full route; null until it's loaded.
 * @property status Where loading the full route is.
 * @property isHandedOff Whether the route was handed off to the maps app, so the rating step may take its place when the user comes back.
 */
data class OneRouteStep(
    val origin: OneRouteOrigin,
    val route: Route? = null,
    val status: OneRouteStatus = OneRouteStatus.Loading,
    val isHandedOff: Boolean = false,
)

/**
 * The step after its route was saved again as [savedRoute]: a saved route's step is about that saved route from then on, and a
 * past drive's step stays about its drive. Unchanged for another route.
 */
fun OneRouteStep.savedAs(routeId: String, savedRoute: SavedRoute): OneRouteStep {
    if (route?.id != routeId || origin !is OneRouteOrigin.Saved) return this
    return copy(origin = OneRouteOrigin.Saved(savedRoute))
}

/** Where the one-route step's route comes from. */
sealed interface OneRouteOrigin {
    /** The saved route's ID, or the drive's. */
    val id: String
    val start: Place
    val destination: Place

    /** A saved route. Saving the route again gives it a new saved route, which replaces this. */
    data class Saved(val savedRoute: SavedRoute) : OneRouteOrigin {
        override val id: String get() = savedRoute.id
        override val start: Place get() = savedRoute.start
        override val destination: Place get() = savedRoute.destination
    }

    /** A drive from Trip history. */
    data class Past(val drive: PastDrive) : OneRouteOrigin {
        override val id: String get() = drive.id
        override val start: Place get() = drive.start
        override val destination: Place get() = drive.destination
    }
}

/** Where loading a one-route step's full route is. */
enum class OneRouteStatus {
    /** Waiting for the route. */
    Loading,

    /** The route is loaded. */
    Ready,

    /** The route isn't on the device and there's no connection. */
    Offline,

    /** Loading failed for another reason. */
    Failed,
}
