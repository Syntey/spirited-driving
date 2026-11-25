package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.distanceText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import com.syntey.spiriteddriving.feature.map.model.ui.BudgetChip
import com.syntey.spiriteddriving.feature.map.model.ui.NoFunRouteMessage
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RouteLine
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import com.syntey.spiriteddriving.feature.map.model.ui.SheetStep
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.UseBudgetButton
import com.syntey.spiriteddriving.feature.map.model.ui.routeRow
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.tools.boundsOf
import com.syntey.spiriteddriving.feature.map.tools.budgetText
import com.syntey.spiriteddriving.feature.map.tools.distanceChips
import com.syntey.spiriteddriving.feature.map.tools.midpointAlong
import com.syntey.spiriteddriving.feature.map.tools.withMinimumSpan
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.math.ceil
import kotlin.time.DurationUnit
import kotlin.time.Duration.Companion.minutes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * What route planning shows, and what it puts on the map.
 *
 * @property planning The trip: its start, destination and budget, the plan and the selected route.
 * @property budgetChoice The budget planning starts with, and the last value in each unit.
 * @property distanceUnit The unit distances are shown in.
 * @property userLocation The user's location once it's known, which a trip without a picked start plans from and frames.
 * @property startSearch The start search, open over planning; null otherwise.
 */
data class RoutePlanningViewState(
    val planning: RoutePlanningState,
    val budgetChoice: BudgetChoice,
    val distanceUnit: DistanceUnit,
    val userLocation: GeoPoint? = null,
    val startSearch: StartSearch? = null,
) : SheetStep {
    override val start: GeoPoint? get() = planning.start?.location

    override val destination: GeoPoint get() = planning.destination.location

    override val routeBounds: GeoBounds
        get() {
            val points = if (planning.plan != null) planning.listedRoutes.flatMap { it.line } else emptyList()
            val bounds = if (points.isEmpty()) planningBounds() else boundsOf(points)
            return bounds.withMinimumSpan()
        }

    override val routeLines: ImmutableList<RouteLine>
        get() {
            return planning.listedRoutes.mapIndexedNotNull { index, route ->
                // A line without points has nothing to draw, and no midpoint
                if (route.line.isEmpty()) return@mapIndexedNotNull null
                // Same numbering as routeRows
                val number = (index + 1).takeIf { route.kind == RouteKind.Fun }
                val points = route.line.toImmutableList()
                RouteLine(
                    id = route.id,
                    number = number,
                    points = points,
                    label = if (number == null) {
                        StringDesc.ResourceFormatted(MR.strings.planning_fastest_label, durationText(route.duration))
                    } else {
                        StringDesc.ResourceFormatted(
                            MR.strings.planning_route_label,
                            number,
                            requireNotNull(extraText(route, planning.budget)) { "A fun route has an extra over the fastest" },
                        )
                    },
                    labelPoint = midpointAlong(route.line),
                    isSelected = route.id == planning.selectedRouteId,
                )
            }.sortedBy { it.isSelected }.toImmutableList()
        }

    override val driveRoute: Route? get() = planning.selectedRoute

    override val driveRouteNumber: Int? get() = selectedRouteRow?.number

    override fun routeWithId(routeId: String): Route? = planning.listedRoutes.firstOrNull { it.id == routeId }

    /** The unit the budget is in: the planning's budget's. */
    val budgetKind: BudgetKind
        get() = when (planning.budget) {
            is Budget.Time -> BudgetKind.Time
            is Budget.Distance -> BudgetKind.Distance
        }

    /** The chips in the planning budget's unit. */
    val budgetChips: ImmutableList<BudgetChip>
        get() {
            val budget = planning.budget
            return budgetOptions(budget, distanceUnit).map { option ->
                BudgetChip(budget = option, label = budgetText(option, distanceUnit), isSelected = option == budget)
            }.toImmutableList()
        }

    /**
     * The message above the fastest route when no fun route fits the budget. Null unless planned without fun routes, so it
     * never shows while planning or with fun routes listed.
     */
    val noFunRouteMessage: NoFunRouteMessage?
        get() {
            val plan = planning.plan ?: return null
            if (planning.status != PlanningStatus.Planned || plan.funRoutes.isNotEmpty()) return null
            return noFunRouteMessageFor(plan)
        }

    /** The routes of the plan as the route list shows them; empty without a plan. */
    val routeRows: ImmutableList<RouteRow>
        get() {
            // The fun routes come first in the listed routes, so a fun route's number is its place in the list
            return planning.listedRoutes.mapIndexed { index, route ->
                routeRow(
                    route = route,
                    distanceUnit = distanceUnit,
                    number = (index + 1).takeIf { route.kind == RouteKind.Fun },
                    extra = extraText(route, planning.budget),
                    isSelected = route.id == planning.selectedRouteId,
                )
            }.toImmutableList()
        }

    /** The selected route as the route list shows it; null without a plan. */
    val selectedRouteRow: RouteRow? get() = routeRows.firstOrNull { it.isSelected }

    private fun planningBounds(): GeoBounds {
        val destination = planning.destination.location
        val origin = planning.start?.location ?: userLocation ?: return GeoBounds(destination, destination)
        return GeoBounds(origin, origin).including(destination)
    }

    // The fastest route is the reference, so it has no extra over itself
    private fun extraText(route: Route, budget: Budget): StringDesc? {
        if (route.kind == RouteKind.Fastest) return null
        return when (budget) {
            is Budget.Time -> StringDesc.ResourceFormatted(MR.strings.planning_extra, durationText(route.extraDuration))

            is Budget.Distance -> StringDesc.ResourceFormatted(
                MR.strings.planning_extra,
                distanceText(route.extraDistance, distanceUnit, isApproximate = false),
            )
        }
    }

    // The closest fun route's budget picks the smallest chip that fits it. A budget in the other unit fits nothing, as with none.
    private fun noFunRouteMessageFor(plan: TripPlan): NoFunRouteMessage {
        val budget = planning.budget
        val options = budgetOptions(budget, distanceUnit)
        val closest = plan.closestFunRouteBudget
        val fitting = closest?.let { needed -> options.firstOrNull { it.allowsAtLeast(needed) } }
        return if (closest != null && fitting != null) {
            NoFunRouteMessage(
                text = StringDesc.ResourceFormatted(
                    MR.strings.planning_no_fun_route,
                    budgetText(budget, distanceUnit),
                    closestText(closest),
                ),
                useBudget = UseBudgetButton(
                    budget = fitting,
                    label = StringDesc.ResourceFormatted(MR.strings.planning_use_budget, budgetText(fitting, distanceUnit)),
                ),
            )
        } else {
            NoFunRouteMessage(
                text = StringDesc.ResourceFormatted(
                    MR.strings.planning_no_fun_route_beyond,
                    budgetText(budget, distanceUnit),
                    budgetText(options.last(), distanceUnit),
                ),
                useBudget = null,
            )
        }
    }

    private fun closestText(closest: Budget): StringDesc = when (closest) {
        is Budget.Time -> {
            // Rounded up to whole minutes, so the text never names a value the current chip already covers
            val wholeMinutes = ceil(closest.extra.toDouble(DurationUnit.MINUTES)).toInt().minutes
            StringDesc.ResourceFormatted(MR.strings.planning_extra, durationText(wholeMinutes))
        }

        is Budget.Distance -> StringDesc.ResourceFormatted(
            MR.strings.planning_extra,
            distanceText(closest.extra, distanceUnit, isApproximate = false),
        )
    }
}

// The chips of the budget's unit, the distance ones in [unit]
private fun budgetOptions(budget: Budget, unit: DistanceUnit): List<Budget> = when (budget) {
    is Budget.Time -> Constants.Budgets.TIME
    is Budget.Distance -> distanceChips(unit)
}

// Whether this budget allows at least [other]'s extra; never across units
private fun Budget.allowsAtLeast(other: Budget): Boolean = when (this) {
    is Budget.Time -> other is Budget.Time && extra >= other.extra
    is Budget.Distance -> other is Budget.Distance && extra >= other.extra
}
