package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
}
