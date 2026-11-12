package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

internal object TripPlanScreenPreviews {
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
        val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)
        val state = viewState()
        return state.copy(
            sheetHeight = SheetHeight.Full,
            map = state.map.copy(userLocation = prague),
            search = SearchState(text = "karlst", suggestions = FakePlaceSearch.Recorded.toImmutableList()),
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
