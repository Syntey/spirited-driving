package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object TripPlanScreenPreviews {
    fun screen(state: TripPlanViewState = viewState()): TripPlanScreen = object : TripPlanScreen {
        override val viewState: StateFlow<TripPlanViewState> = MutableStateFlow(state)
        override val actions: TripPlanScreen.Actions = TripPlanScreen.Actions.noOpActions()
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
