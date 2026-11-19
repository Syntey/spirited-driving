package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SearchStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripPlanComponentTest {

    private val main = TripPlanTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `typing a place shows its suggestions`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(SearchStatus.Found, state.search.status)
        assertEquals(
            FakePlaceSearch.Recorded.map { it.place.name },
            state.suggestionRows.map { it.suggestion.place.name },
        )
    }

    @Test
    fun `text shorter than the minimum shows no suggestions`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        trip.screen.actions.onSearchTextChange("k")
        advanceUntilIdle()

        val search = trip.screen.viewState.value.search
        assertEquals(SearchStatus.Idle, search.status)
        assertTrue(search.suggestions.isEmpty())
    }

    @Test
    fun `text shorter than the minimum doesn't search`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        trip.screen.actions.onSearchTextChange("k")
        advanceUntilIdle()

        // The place search backend is never asked, so nothing could have answered
        assertTrue(trip.placeSearch.searchedTexts.isEmpty())
    }

    @Test
    fun `an offline search shows offline`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.placeSearch.failure = OfflineException(cause = null)

        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()

        assertEquals(SearchStatus.Offline, trip.screen.viewState.value.search.status)
    }

    @Test
    fun `Retry after an offline search shows suggestions`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.placeSearch.failure = OfflineException(cause = null)
        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()

        trip.placeSearch.failure = null
        trip.screen.actions.onSearchRetry()
        advanceUntilIdle()

        val search = trip.screen.viewState.value.search
        assertEquals(SearchStatus.Found, search.status)
        assertEquals(FakePlaceSearch.Recorded.size, search.suggestions.size)
    }

    @Test
    fun `clearing the search empties it`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()

        trip.screen.actions.onSearchClear()
        advanceUntilIdle()

        val search = trip.screen.viewState.value.search
        assertEquals("", search.text)
        assertEquals(SearchStatus.Idle, search.status)
        assertTrue(search.suggestions.isEmpty())
    }

    @Test
    fun `clearing the search keeps recent places`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(recentPlaces = listOf(karlstejn))
        advanceUntilIdle()
        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()

        trip.screen.actions.onSearchClear()
        advanceUntilIdle()

        assertEquals(listOf(karlstejn), trip.screen.viewState.value.recentRows.map { it.place })
    }

    @Test
    fun `recent places show when the map opens`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(recentPlaces = listOf(karlstejn, vinohradska))
        advanceUntilIdle()

        assertEquals(listOf(karlstejn, vinohradska), trip.screen.viewState.value.recentRows.map { it.place })
    }

    @Test
    fun `removing a recent place hides it at once`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(recentPlaces = listOf(karlstejn, vinohradska))
        advanceUntilIdle()

        trip.screen.actions.onRecentPlaceRemove(karlstejn)

        // Checked before the removal is saved, as the row must not wait for it
        assertEquals(listOf(vinohradska), trip.screen.viewState.value.recentRows.map { it.place })
    }

    @Test
    fun `a removed recent place stays removed once saved`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(recentPlaces = listOf(karlstejn, vinohradska))
        advanceUntilIdle()
        trip.screen.actions.onRecentPlaceRemove(karlstejn)

        advanceUntilIdle()

        assertEquals(listOf(vinohradska), trip.screen.viewState.value.recentRows.map { it.place })
    }

    @Test
    fun `choosing a suggestion outside the covered area shows the not-covered event`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()
        val outside = trip.screen.viewState.value.suggestionRows.first { !it.suggestion.isCovered }.suggestion

        trip.screen.actions.onSuggestionClick(outside)
        advanceUntilIdle()

        assertEquals(listOf<TripPlanUiEvent>(TripPlanUiEvent.NotCovered), trip.events)
    }

    @Test
    fun `choosing a suggestion outside the covered area doesn't plan`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()
        trip.screen.actions.onSearchTextChange("karlst")
        advanceUntilIdle()
        val outside = trip.screen.viewState.value.suggestionRows.first { !it.suggestion.isCovered }.suggestion

        trip.screen.actions.onSuggestionClick(outside)
        advanceUntilIdle()

        assertNull(trip.screen.viewState.value.planning)
    }

    @Test
    fun `choosing a suggestion with a usable location plans the trip and shows its routes`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        val chosen = searchForCovered(trip)

        trip.screen.actions.onSuggestionClick(chosen)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(chosen.place, state.planning?.destination)
        assertEquals(PlanningStatus.Planned, state.planning?.status)
        // The fun routes are listed first, numbered from 1; the fastest route follows without a number
        assertEquals(listOf<Int?>(1, null), state.routeRows.map { it.number })
        assertEquals(SheetHeight.Half, state.sheetHeight)
        assertNull(state.startSearch)
    }

    @Test
    fun `choosing a place without location access opens the start search`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Denied)
        advanceUntilIdle()
        val chosen = searchForCovered(trip)

        trip.screen.actions.onSuggestionClick(chosen)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNotNull(state.startSearch)
        assertEquals(chosen.place, state.planning?.destination)
        assertNull(state.planning?.plan)
        assertEquals(SheetHeight.Full, state.sheetHeight)
        assertEquals(MyLocationRow.NeedsLocationAccess, state.myLocationRow)
    }

    @Test
    fun `closing that start search closes planning`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture(locationAccess = LocationAccess.Denied)
        advanceUntilIdle()
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()

        trip.screen.actions.onSearchClose()
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertNull(state.startSearch)
        assertNull(state.planning)
    }

    @Test
    fun `the profile button opens Profile`() = runTest(main.dispatcher) {
        val trip = tripPlanFixture()
        advanceUntilIdle()

        trip.screen.actions.onProfileClick()

        assertEquals(listOf(RecordingTripPlanNavigation.Call.Profile), trip.navigation.calls)
    }
}

private val karlstejn = Place(
    name = "Karlštejn",
    area = "Budňany, Central Bohemian Region",
    location = Points.pinned,
)

private val vinohradska = Place(
    name = "Vinohradská 12",
    area = "Prague 2",
    location = Points.prague,
)
