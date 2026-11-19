package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteSummary
import kotlin.time.Instant

/**
 * A route the user saved, for lists. Its full route comes from [SavedRoutesSource.getRoute].
 *
 * @property id The saved route's own ID, not its route's.
 * @property route The summary of the [Route] it keeps, as planned; its ID is the route's ID.
 */
data class SavedRoute(val id: SavedRouteId, val name: String, val savedAt: Instant, val route: RouteSummary)
