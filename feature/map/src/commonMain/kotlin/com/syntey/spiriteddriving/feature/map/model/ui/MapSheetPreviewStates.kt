package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.ActiveSheetStep
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetUiEvent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
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
internal object MapSheetPreviewStates {
    fun screen(
        state: MapSheetViewState = viewState(),
        step: ActiveSheetStep = ActiveSheetStep.Search,
    ): MapSheetScreen = object : MapSheetScreen {
        override val viewState: StateFlow<MapSheetViewState> = MutableStateFlow(state)
        override val activeStep: StateFlow<ActiveSheetStep> = MutableStateFlow(step)
        override val actions: MapSheetScreen.Actions = MapSheetScreen.Actions.noOpActions()
        override val events: Flow<MapSheetUiEvent> = emptyFlow()

        override fun onSignInToSaveFinished(isSignedIn: Boolean) = Unit
        override fun showSavedRoute(savedRouteId: SavedRouteId) = Unit
        override fun showPastDrive(driveId: String) = Unit
        override fun planNewRoutes(driveId: String) = Unit
    }

    fun viewState(locationAccess: LocationAccess? = LocationAccess.Precise, followsUser: Boolean = true) = MapSheetViewState(
        locationAccess = locationAccess,
        baseMap = RouteMapState(showsUserLocation = locationAccess.allowsLocation, followsUser = followsUser),
    )

    fun approximateViewState(): MapSheetViewState =
        viewState(locationAccess = LocationAccess.Approximate).copy(isPreciseLocationNoteClosed = false)

    fun searchViewState(): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", suggestions = FakePlaceSearch.Recorded.toImmutableList()),
    )

    fun offlineViewState(): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", status = SearchStatus.Offline),
    )

    fun failedViewState(): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", status = SearchStatus.Failed),
    )

    fun noResultsViewState(): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlsxyz", status = SearchStatus.Found),
    )

    fun slowSearchViewState(): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(
            text = "karlst",
            suggestions = FakePlaceSearch.Recorded.toImmutableList(),
            status = SearchStatus.Searching,
            isSlow = true,
        ),
    )

    /** Prague as the user's location, so the rows show distances, and one place is not covered. */
    fun searchDistancesViewState(): MapSheetViewState {
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Full,
            baseMap = state.baseMap.copy(userLocation = PRAGUE),
            search = SearchState(text = "karlst", suggestions = FakePlaceSearch.Recorded.toImmutableList()),
        )
    }

    /** The field is blank: the recent places, with Prague as the user's location. */
    fun recentViewState(): MapSheetViewState {
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Half,
            baseMap = state.baseMap.copy(userLocation = PRAGUE),
            search = SearchState(recentPlaces = recentPlaces),
        )
    }

    /** A search that matches a recent place: the castle shows once, as a recent row. */
    fun recentSearchViewState(): MapSheetViewState {
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Full,
            baseMap = state.baseMap.copy(userLocation = PRAGUE),
            search = SearchState(
                text = "karlst",
                suggestions = FakePlaceSearch.Recorded.toImmutableList(),
                status = SearchStatus.Found,
                recentPlaces = recentPlaces,
            ),
        )
    }

    fun recentOfflineViewState(): MapSheetViewState = viewState().copy(
        sheetHeight = SheetHeight.Full,
        search = SearchState(text = "karlst", status = SearchStatus.Offline, recentPlaces = recentPlaces),
    )

    private val PRAGUE = GeoPoint(latitude = 50.0755, longitude = 14.4378)

    internal val recentPlaces = listOf(
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

    // The view state test reads these two from here; the others are in RoutePlanningPreviewStates
    fun planningViewState(): MapSheetViewState = RoutePlanningPreviewStates.planningViewState()

    /** A plan with three fun routes and the fastest one; route 1 is selected. */
    fun plannedViewState(): MapSheetViewState = RoutePlanningPreviewStates.plannedViewState()

    /** The reminder over the collapsed search, for a named destination or an unnamed one, which reads as a point on the map. */
    fun reminderViewState(destinationName: String? = "Karlštejn Castle"): MapSheetViewState {
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

    fun outsideViewState(): MapSheetViewState {
        val covered = FakeCoverageSource.Czechia
        val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)
        return MapSheetViewState(
            locationAccess = LocationAccess.Precise,
            baseMap = RouteMapState(
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
