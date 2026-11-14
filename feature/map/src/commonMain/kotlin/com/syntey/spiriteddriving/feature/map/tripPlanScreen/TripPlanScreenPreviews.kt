package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
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
    ).toImmutableList()

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
