package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanUiEvent
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
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

        override fun onSignInToSaveFinished(isSignedIn: Boolean) = Unit
        override fun showSavedRoute(savedRouteId: String) = Unit
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

    /** The start step over route planning to Karlštejn Castle, with the recent places and the My location row for [locationAccess]. */
    fun startStepViewState(locationAccess: LocationAccess? = LocationAccess.Precise): TripPlanViewState {
        val state = viewState(locationAccess = locationAccess)
        return state.copy(
            sheetHeight = SheetHeight.Full,
            map = state.map.copy(userLocation = PRAGUE),
            planning = RoutePlanningState(destination = DESTINATION, budget = BUDGET),
            startSearch = StartSearch(destinationSearch = SearchState(), closesPlanning = false),
            search = SearchState(recentPlaces = recentPlaces),
        )
    }

    /** A plan to Karlštejn Castle from a point picked as the start: the header says "From Beroun". */
    fun plannedFromPickedStartViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, start = BEROUN, budget = BUDGET).withPlan(plannedPlan),
    )

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

    /** The plan of [plannedViewState] with a distance budget in miles, so the picker shows the mile chips. */
    fun plannedMilesViewState(): TripPlanViewState = planningState(
        RoutePlanningState(destination = DESTINATION, budget = Budget.Distance(20.miles)).withPlan(plannedPlan),
    ).copy(distanceUnit = DistanceUnit.Miles)

    /** [plannedViewState] with the user about 12 km north of the start, so Drive says the maps app takes them there first. */
    fun plannedFarStartViewState(): TripPlanViewState = plannedViewState().let { state ->
        state.copy(map = state.map.copy(userLocation = FAR_FROM_START))
    }

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

    /** A saved route's step: loaded from the Recorded saved route, which is saved, so Saved shows. */
    fun savedRouteViewState(
        status: SavedRouteStatus = SavedRouteStatus.Ready,
        sheetHeight: SheetHeight = SheetHeight.Half,
    ): TripPlanViewState {
        val saved = FakeSavedRoutesSource.Recorded
        val step = SavedRouteStep(
            savedRoute = saved,
            route = FakeRoutePlanner.Recorded.funRoutes.first().takeIf { status == SavedRouteStatus.Ready },
            status = status,
        )
        return viewState().copy(
            savedRouteStep = step,
            sheetHeight = sheetHeight,
            savedRoutes = listOf(saved).toImmutableList(),
        )
    }

    private fun planningState(planning: RoutePlanningState): TripPlanViewState = viewState().copy(
        sheetHeight = SheetHeight.Half,
        map = RouteMapState(showsUserLocation = true, userLocation = PRAGUE_START),
        planning = planning,
    )

    private val DESTINATION: Place = FakeRoutePlanner.Recorded.fastest.destination
    private val PRAGUE_START = GeoPoint(latitude = 50.0755, longitude = 14.4378)
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

    /** The rating step after a drive to Karlštejn Castle along the first fun route, with [rating] picked so far. */
    fun ratingViewState(rating: Rating? = null, showsReminderOffer: Boolean = false): TripPlanViewState {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val question = DriveQuestion(
            driveId = "preview",
            startedAt = Instant.parse("2026-11-16T13:20:00Z"),
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination,
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        return viewState().copy(
            sheetHeight = SheetHeight.Half,
            ratingStep = RatingStep(
                question = question,
                driveTime = "Today, 14:20".desc(),
                rating = rating,
                showsReminderOffer = showsReminderOffer,
            ),
        )
    }

    /** The reminder over the collapsed search, for a named destination or an unnamed one, which reads as a point on the map. */
    fun reminderViewState(destinationName: String? = "Karlštejn Castle"): TripPlanViewState {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val question = DriveQuestion(
            driveId = "preview",
            startedAt = Instant.parse("2026-11-15T13:20:00Z"),
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination.copy(name = destinationName),
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        return viewState().copy(
            reminder = ReminderBubble(question = question, day = MR.strings.drive_feedback_yesterday_day.desc()),
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
