package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanUiEvent
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import kotlin.time.Duration.Companion.minutes
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/** Sample view states that the previews share: one per sheet state, so the object has more functions than the limit. */
@Suppress("TooManyFunctions")
internal object TripPlanPreviewStates {
    fun screen(state: TripPlanViewState = viewState()): TripPlanScreen = object : TripPlanScreen {
        override val viewState: StateFlow<TripPlanViewState> = MutableStateFlow(state)
        override val actions: TripPlanScreen.Actions = TripPlanScreen.Actions.noOpActions()
        override val events: Flow<TripPlanUiEvent> = emptyFlow()
    }

    fun viewState(locationAccess: LocationAccess? = LocationAccess.Precise, followsUser: Boolean = true) = TripPlanViewState(
        locationAccess = locationAccess,
        map = RouteMapState(showsUserLocation = locationAccess.allowsLocation, followsUser = followsUser),
    )

    fun approximateViewState(): TripPlanViewState =
        viewState(locationAccess = LocationAccess.Approximate).copy(isPreciseLocationNoteClosed = false)

    fun searchViewState(): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", suggestions = FakePlaceSearch.Recorded.toImmutableList()),
    )

    fun offlineViewState(): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", status = SearchStatus.Offline),
    )

    fun failedViewState(): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", status = SearchStatus.Failed),
    )

    fun noResultsViewState(): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlsxyz", status = SearchStatus.Found),
    )

    fun slowSearchViewState(): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(
            text = "karlst",
            suggestions = FakePlaceSearch.Recorded.toImmutableList(),
            status = SearchStatus.Searching,
            isSlow = true,
        ),
    )

    /** Prague as the user's location, so the rows show distances, and one place is not covered. */
    fun searchDistancesViewState(): TripPlanViewState {
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Full,
            map = state.map.copy(userLocation = PRAGUE),
            search = SearchState(text = "karlst", suggestions = FakePlaceSearch.Recorded.toImmutableList()),
        )
    }

    /** The field is blank: the recent places, with Prague as the user's location. */
    fun recentViewState(): TripPlanViewState {
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Half,
            map = state.map.copy(userLocation = PRAGUE),
            search = SearchState(recentPlaces = recentPlaces),
        )
    }

    /** A search that matches a recent place: the castle shows once, as a recent row. */
    fun recentSearchViewState(): TripPlanViewState {
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Full,
            map = state.map.copy(userLocation = PRAGUE),
            search = SearchState(
                text = "karlst",
                suggestions = FakePlaceSearch.Recorded.toImmutableList(),
                status = SearchStatus.Found,
                recentPlaces = recentPlaces,
            ),
        )
    }

    fun recentOfflineViewState(): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", status = SearchStatus.Offline, recentPlaces = recentPlaces),
    )

    private val PRAGUE = GeoPoint(latitude = 50.0755, longitude = 14.4378)

    private val recentPlaces = listOf(
        FakePlaceSearch.Recorded[1].place,
        Place(
            name = "Křivoklát",
            area = "Rakovník, Central Bohemian Region",
            location = GeoPoint(latitude = 50.0378, longitude = 13.8717),
        ),
        Place(
            name = "Václavské náměstí",
            area = "Prague",
            location = GeoPoint(latitude = 50.0810, longitude = 14.4280),
        ),
        Place(
            name = null,
            area = "49.9394, 14.1882",
            location = GeoPoint(latitude = 49.9394, longitude = 14.1882),
        ),
    ).toImmutableList()

    /** Route planning to Karlštejn Castle, while the plan is on its way. */
    fun planningViewState(): TripPlanViewState = planningState(RoutePlanningState(destination = DESTINATION, budget = BUDGET))

    /** A plan with three fun routes and the fastest one; route 1 is selected. */
    fun plannedViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET).withPlan(plannedPlan),
    )

    /** [plannedViewState] with the sheet collapsed: the selected route and the actions. */
    fun plannedCollapsedViewState(): TripPlanViewState = plannedViewState().copy(sheetHeight = SheetHeight.Collapsed)

    /** [planningViewState] with the sheet collapsed: the spinner and the actions. */
    fun planningCollapsedViewState(): TripPlanViewState = planningViewState().copy(sheetHeight = SheetHeight.Collapsed)

    /** The plan of [plannedViewState] with a distance budget, so the picker shows the km chips. */
    fun plannedDistanceViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Distance(50.kilometres)).withPlan(plannedPlan),
    )

    /**
     * A plan without a fun route that fits +15 min, where the closest one needs +22 min: only the fastest route is listed, and
     * it is selected. The message offers the +30 min chip.
     */
    fun noFunRouteViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Time(15.minutes))
            .withPlan(FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = Budget.Time(22.minutes))),
    )

    /** A plan without a fun route within +2 h, the largest chip: the message has no button. */
    fun noFunRouteBeyondViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Time(15.minutes))
            .withPlan(FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = null)),
    )

    fun planningOfflineViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET, status = PlanningStatus.Offline),
    )

    fun planningNotCoveredViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET, status = PlanningStatus.NotCovered),
    )

    fun planningFailedViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = BUDGET, status = PlanningStatus.Failed),
    )

    private fun planningState(planning: RoutePlanningState): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Half,
        map = RouteMapState(showsUserLocation = true, userLocation = PRAGUE_START),
        planning = planning,
    )

    private val DESTINATION: Place = FakeRoutePlanner.Recorded.fastest.destination
    private val PRAGUE_START = GeoPoint(latitude = 50.0755, longitude = 14.4378)
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

    fun outsideViewState(): TripPlanViewState {
        val covered = FakeCoverageSource.Czechia
        val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)
        return TripPlanViewState(
            locationAccess = LocationAccess.Precise,
            map = RouteMapState(
                showsUserLocation = true,
                followsUser = false,
                userLocation = dresden,
                overview = covered.bounds.including(dresden),
                coveredArea = covered.area,
                overviewRequest = 1,
            ),
        )
    }
}
