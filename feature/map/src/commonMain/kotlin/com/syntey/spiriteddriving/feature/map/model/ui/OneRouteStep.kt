package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.feature.map.tools.boundsOf
import com.syntey.spiriteddriving.feature.map.tools.midpointAlong
import com.syntey.spiriteddriving.feature.map.tools.withMinimumSpan
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * The one-route step of the sheet: a route shown on its own, ready to drive, from Saved routes or Trip history.
 *
 * @property origin Where the route comes from.
 * @property route The full route; null until it's loaded.
 * @property status Where loading the full route is.
 */
data class OneRouteStep(val origin: OneRouteOrigin, val route: Route? = null, val status: OneRouteStatus = OneRouteStatus.Loading) :
    SheetStep {
    override val start: GeoPoint get() = origin.start.location

    override val destination: GeoPoint get() = origin.destination.location

    override val routeBounds: GeoBounds
        get() {
            // Until the route is loaded, the start and destination of its origin frame the map
            val line = route?.line.orEmpty()
            return boundsOf(line.ifEmpty { listOf(origin.start.location, origin.destination.location) }).withMinimumSpan()
        }

    override val routeLines: ImmutableList<RouteLine>
        get() {
            val line = route?.line.orEmpty()
            if (line.isEmpty()) return persistentListOf()
            return listOf(
                RouteLine(
                    id = origin.id,
                    number = 1,
                    points = line.toImmutableList(),
                    label = null,
                    labelPoint = midpointAlong(line),
                    isSelected = true,
                ),
            ).toImmutableList()
        }

    override val driveRoute: Route? get() = route
}

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
        override val id: String get() = savedRoute.id.value
        override val start: Place get() = savedRoute.route.start
        override val destination: Place get() = savedRoute.route.destination
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
