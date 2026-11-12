package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.feature.map.model.ui.SearchFailure
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.toImmutableList

class SearchStateTest {

    @Test
    fun `hint shows while the field is empty`() {
        assertTrue(SearchState().showsHint)
    }

    @Test
    fun `hint hides once there is text`() {
        assertFalse(SearchState(text = "k").showsHint)
    }

    @Test
    fun `no results shows once a long enough text is found to match nothing`() {
        assertTrue(SearchState(text = "karlst", status = SearchStatus.Found).showsNoResults)
    }

    @Test
    fun `no results hides while the search runs`() {
        assertFalse(SearchState(text = "karlst", status = SearchStatus.Searching).showsNoResults)
    }

    @Test
    fun `no results hides for text under two characters`() {
        assertFalse(SearchState(text = "k", status = SearchStatus.Found).showsNoResults)
    }

    @Test
    fun `no results hides when there are suggestions`() {
        val state = SearchState(
            text = "karlst",
            suggestions = FakePlaceSearch.Recorded.toImmutableList(),
            status = SearchStatus.Found,
        )

        assertFalse(state.showsNoResults)
    }

    @Test
    fun `progress shows while a search is slow`() {
        assertTrue(SearchState(text = "karlst", status = SearchStatus.Searching, isSlow = true).showsProgress)
    }

    @Test
    fun `progress hides while a search is quick`() {
        assertFalse(SearchState(text = "karlst", status = SearchStatus.Searching, isSlow = false).showsProgress)
    }

    @Test
    fun `progress hides once the search has answered`() {
        assertFalse(SearchState(text = "karlst", status = SearchStatus.Found, isSlow = true).showsProgress)
    }

    @Test
    fun `a blank field shows all recent places and the header`() {
        val state = SearchState(recentPlaces = recents)

        assertEquals(recents, state.shownRecentPlaces)
        assertTrue(state.showsRecentHeader)
    }

    @Test
    fun `typing shows at most three matching recent places`() {
        val state = SearchState(
            text = "a",
            recentPlaces = (1..5).map { place("Alfa $it") }.toImmutableList(),
        )

        assertEquals(3, state.shownRecentPlaces.size)
    }

    @Test
    fun `typing hides the header`() {
        assertFalse(SearchState(text = "k", recentPlaces = recents).showsRecentHeader)
    }

    @Test
    fun `a suggestion that is the same place as a shown recent one is left out`() {
        val state = SearchState(
            text = "karlst",
            suggestions = FakePlaceSearch.Recorded.toImmutableList(),
            status = SearchStatus.Found,
            recentPlaces = listOf(FakePlaceSearch.Recorded[1].place).toImmutableList(),
        )

        assertEquals(FakePlaceSearch.Recorded.minus(FakePlaceSearch.Recorded[1]), state.shownSuggestions)
    }

    @Test
    fun `a suggestion that is the same place as a recent one not shown stays`() {
        val state = SearchState(
            text = "karlst",
            suggestions = FakePlaceSearch.Recorded.toImmutableList(),
            status = SearchStatus.Found,
            recentPlaces = listOf(place("Václavské náměstí")).toImmutableList(),
        )

        assertEquals(FakePlaceSearch.Recorded, state.shownSuggestions)
    }

    @Test
    fun `the hint shows while the field is blank and there are no recent places`() {
        assertTrue(SearchState().showsHint)
    }

    @Test
    fun `the hint hides with recent places`() {
        assertFalse(SearchState(recentPlaces = recents).showsHint)
    }

    @Test
    fun `no results hides when a recent place matches`() {
        val state =
            SearchState(text = "karlst", status = SearchStatus.Found, recentPlaces = recents)

        assertFalse(state.showsNoResults)
    }

    @Test
    fun `with one character matching recent places still show`() {
        val state = SearchState(text = "k", status = SearchStatus.Idle, recentPlaces = recents)

        assertEquals(recents.take(2), state.shownRecentPlaces)
    }

    @Test
    fun `failure is offline for an offline search`() {
        assertEquals(
            SearchFailure.Offline,
            SearchState(
                text = "karlst",
                status = SearchStatus.Offline,
            ).failure,
        )
    }

    @Test
    fun `failure is failed for a failed search`() {
        assertEquals(
            SearchFailure.Failed,
            SearchState(
                text = "karlst",
                status = SearchStatus.Failed,
            ).failure,
        )
    }

    @Test
    fun `failure is none once the search has found`() {
        assertNull(SearchState(text = "karlst", status = SearchStatus.Found).failure)
    }

    @Test
    fun `failure shows alone without matching recent places`() {
        assertTrue(SearchState(text = "karlst", status = SearchStatus.Offline).showsFailureAlone)
    }

    @Test
    fun `failure does not show alone when a recent place matches`() {
        val state = SearchState(
            text = "karl",
            status = SearchStatus.Offline,
            recentPlaces = listOf(place("Karlštejn")).toImmutableList(),
        )

        assertFalse(state.showsFailureAlone)
    }

    private val recents = listOf(place("Karlštejn"), place("Křivoklát"), place("Václavské náměstí")).toImmutableList()

    private fun place(name: String) = Place(name = name, area = null, location = GeoPoint(latitude = 50.0, longitude = 14.0))
}
