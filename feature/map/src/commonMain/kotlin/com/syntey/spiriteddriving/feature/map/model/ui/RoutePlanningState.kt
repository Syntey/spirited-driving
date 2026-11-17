package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.TripPlan

/**
 * The route planning step of the sheet, from [start], or the user's location, to [destination].
 *
 * @property start The start picked by hand; null starts from the user's location.
 * @property isHandedOff Whether a route of this plan was handed off to the maps app, so the rating step may take its place when
 *   the user comes back.
 */
data class RoutePlanningState(
    val destination: Place,
    val start: Place? = null,
    val budget: Budget,
    val status: PlanningStatus = PlanningStatus.Planning,
    val plan: TripPlan? = null,
    val selectedRouteId: String? = null,
    val isHandedOff: Boolean = false,
) {
    /**
     * The routes as the list shows them: fun routes first, most fun first, then the fastest as the reference. Empty until
     * planned.
     */
    val listedRoutes: List<Route>
        get() = plan?.let { it.funRoutes + it.fastest }.orEmpty()

    /** The selected route, once planned. */
    val selectedRoute: Route?
        get() = listedRoutes.firstOrNull { it.id == selectedRouteId }

    /** The plan arrived: route 1 is selected, or the fastest route when no fun route fits. */
    fun withPlan(plan: TripPlan): RoutePlanningState = copy(
        status = PlanningStatus.Planned,
        plan = plan,
        selectedRouteId = (plan.funRoutes.firstOrNull() ?: plan.fastest).id,
    )
}

/** Where route planning is. */
enum class PlanningStatus {
    /** Waiting for the plan. */
    Planning,

    /** The plan is shown. */
    Planned,

    /** The start or destination is outside the covered area. */
    NotCovered,

    /** Planning failed without a connection. */
    Offline,

    /** Planning failed for another reason. */
    Failed,
}
