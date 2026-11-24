package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.route.Route
import kotlinx.collections.immutable.ImmutableList

/**
 * What the step on top of the sheet puts on the map and hands to Drive and Save: route planning, the rating step, or one route
 * on its own.
 */
interface SheetStep {
    /** The start pin the step puts on the map; null when it has none. */
    val start: GeoPoint?

    /** The destination pin the step puts on the map. */
    val destination: GeoPoint

    /** The area the map frames for the step. */
    val routeBounds: GeoBounds

    /** The step's lines on the map, the selected one last so it draws on top; empty when there are none with points. */
    val routeLines: ImmutableList<RouteLine>

    /** The route Drive hands off and Save keeps; null while there's none, such as while it loads. */
    val driveRoute: Route? get() = null

    /** The number the route list gives [driveRoute]; null for the fastest route and for steps without a list. */
    val driveRouteNumber: Int? get() = null

    /** The route with [routeId] among the routes the step shows; null when there's none. */
    fun routeWithId(routeId: String): Route? = driveRoute?.takeIf { it.id == routeId }
}
