package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.MapSheetPreviewStates.recentPlaces
import com.syntey.spiriteddriving.feature.map.model.ui.MapSheetPreviewStates.viewState
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.ActiveSheetStep
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningViewState
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableStateFlow

/** Sample view states of route planning, which the planning's previews share. */
internal object RoutePlanningPreviewStates {
    private val PRAGUE_START = GeoPoint(latitude = 50.0755, longitude = 14.4378)

    /** The step that shows [state]'s route planning, with a screen over its planning state. */
    fun stepOf(state: MapSheetViewState): ActiveSheetStep.RoutePlanning {
        val planning = requireNotNull(state.sheetStep as? RoutePlanningViewState) { "The sheet shows route planning" }
        return ActiveSheetStep.RoutePlanning(screen(planning))
    }

    /** A route planning screen that shows [planning] and takes no taps. */
    fun screen(planning: RoutePlanningViewState): RoutePlanningScreen = object : RoutePlanningScreen {
        override val viewState: MutableStateFlow<RoutePlanningViewState> = MutableStateFlow(planning)
        override val actions: RoutePlanningScreen.Actions = RoutePlanningScreen.Actions.noOpActions()
    }

    /** The start search over route planning to Karlštejn Castle, with the recent places and the My location row for [locationAccess]. */
    fun startStepViewState(locationAccess: LocationAccess? = LocationAccess.Precise): MapSheetViewState {
        val startSearch = StartSearch(closesPlanning = false)
        val state = viewState(locationAccess = locationAccess)
        return state.copy(
            sheetHeight = SheetHeight.Full,
            baseMap = state.baseMap.copy(userLocation = PRAGUE_START),
            sheetStep = planningOf(RoutePlanningState(destination = DESTINATION, budget = BUDGET), startSearch = startSearch),
            startSearch = startSearch,
            search = SearchState(recentPlaces = recentPlaces),
        )
    }

    /** A plan to Karlštejn Castle from a point picked as the start: the header says "From Beroun". */
    fun plannedFromPickedStartViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, start = BEROUN, budget = BUDGET).withPlan(plannedPlan),
    )

    /** Route planning to Karlštejn Castle, while the plan is on its way. */
    fun planningViewState(): MapSheetViewState = planningState(RoutePlanningState(destination = DESTINATION, budget = BUDGET))

    /** A plan with three fun routes and the fastest one; route 1 is selected. */
    fun plannedViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET).withPlan(plannedPlan),
    )

    /** [plannedViewState] with the sheet collapsed: the selected route and the actions. */
    fun plannedCollapsedViewState(): MapSheetViewState = plannedViewState().copy(sheetHeight = SheetHeight.Collapsed)

    /** [planningViewState] with the sheet collapsed: the spinner and the actions. */
    fun planningCollapsedViewState(): MapSheetViewState = planningViewState().copy(sheetHeight = SheetHeight.Collapsed)

    /** The plan of [plannedViewState] with a distance budget, so the picker shows the km chips. */
    fun plannedDistanceViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Distance(50.kilometres)).withPlan(plannedPlan),
    )

    /** The plan of [plannedViewState] with a distance budget in miles, so the picker shows the mile chips. */
    fun plannedMilesViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Distance(20.miles)).withPlan(plannedPlan),
        distanceUnit = DistanceUnit.Miles,
    )

    /** [plannedViewState] with the user about 12 km north of the start, so Drive says the maps app takes them there first. */
    fun plannedFarStartViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET).withPlan(plannedPlan),
        userLocation = FAR_FROM_START,
    )

    /**
     * A plan without a fun route that fits +15 min, where the closest one needs +22 min: only the fastest route is listed, and
     * it is selected. The message offers the +30 min chip.
     */
    fun noFunRouteViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Time(15.minutes))
            .withPlan(FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = Budget.Time(22.minutes))),
    )

    /** A plan without a fun route within +2 h, the largest chip: the message has no button. */
    fun noFunRouteBeyondViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Time(15.minutes))
            .withPlan(FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = null)),
    )

    fun planningOfflineViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET, status = PlanningStatus.Offline),
    )

    fun planningNotCoveredViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET, status = PlanningStatus.NotCovered),
    )

    fun planningFailedViewState(): MapSheetViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET, status = PlanningStatus.Failed),
    )

    // The sheet with route planning on top, and the planning state the step shows
    private fun planningState(
        planning: RoutePlanningState,
        distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
        userLocation: GeoPoint = PRAGUE_START,
    ): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Half,
        baseMap = RouteMapState(showsUserLocation = true, userLocation = userLocation),
        distanceUnit = distanceUnit,
        sheetStep = planningOf(planning, distanceUnit = distanceUnit, userLocation = userLocation),
    )

    private fun planningOf(
        planning: RoutePlanningState,
        startSearch: StartSearch? = null,
        distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
        userLocation: GeoPoint = PRAGUE_START,
    ): RoutePlanningViewState = RoutePlanningViewState(
        planning = planning,
        budgetChoice = Constants.Budgets.DEFAULT_CHOICE,
        distanceUnit = distanceUnit,
        userLocation = userLocation,
        startSearch = startSearch,
    )

    private val DESTINATION: Place = FakeRoutePlanner.Recorded.fastest.destination
    private val FAR_FROM_START = GeoPoint(latitude = 50.1835, longitude = 14.4378)
    private val BEROUN = Place(
        name = "Beroun",
        area = "Central Bohemia",
        location = GeoPoint(latitude = 49.96, longitude = 14.07),
    )
    private val BUDGET = Budget.Time(45.minutes)

    // The recorded fun route, copied three times with the fun levels of a plan that has three fun routes
    private val plannedPlan: TripPlan = FakeRoutePlanner.Recorded.let { recorded ->
        val funRoute = recorded.funRoutes.single()
        recorded.copy(
            funRoutes = listOf(
                funRoute.copy(id = "route-fun-1", funLevel = 5, extraDuration = 27.minutes),
                funRoute.copy(id = "route-fun-2", funLevel = 4, extraDuration = 33.minutes),
                funRoute.copy(id = "route-fun-3", funLevel = 3, extraDuration = 41.minutes),
            ),
        )
    }
}
