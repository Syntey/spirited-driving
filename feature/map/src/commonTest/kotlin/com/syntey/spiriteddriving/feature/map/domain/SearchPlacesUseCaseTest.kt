package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase.SearchUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SearchPlacesUseCaseTest {

    @Test
    fun `text shorter than two characters emits no suggestions without searching`() = runTest {
        val fake = FakePlaceSearch()

        val updates = SearchPlacesUseCase(fake).build(SearchPlacesUseCase.Args("k", null)).toList()

        assertEquals(listOf<SearchUpdate>(SearchUpdate.Found(emptyList())), updates)
        assertTrue(fake.searches.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `nothing is searched before the pause in typing`() = runTest {
        val fake = FakePlaceSearch()
        val updates = mutableListOf<SearchUpdate>()
        backgroundScope.launch { SearchPlacesUseCase(fake).build(SearchPlacesUseCase.Args("karlst", null)).toList(updates) }

        advanceTimeBy(299.milliseconds)
        assertTrue(fake.searches.isEmpty())

        advanceTimeBy(2.milliseconds)
        assertEquals(1, fake.searches.size)
    }

    @Test
    fun `a quick answer emits only the suggestions`() = runTest {
        val updates = SearchPlacesUseCase(FakePlaceSearch()).build(SearchPlacesUseCase.Args("karlst", null)).toList()

        assertEquals(listOf<SearchUpdate>(SearchUpdate.Found(FakePlaceSearch.Recorded)), updates)
    }

    @Test
    fun `an answer slower than half a second emits slow before the suggestions`() = runTest {
        val updates = SearchPlacesUseCase(SlowPlaceSearch(answerAfter = 700.milliseconds))
            .build(SearchPlacesUseCase.Args("karlst", null))
            .toList()

        assertEquals(listOf(SearchUpdate.Slow, SearchUpdate.Found(FakePlaceSearch.Recorded)), updates)
    }

    @Test
    fun `an answer just under half a second emits no slow`() = runTest {
        val updates = SearchPlacesUseCase(SlowPlaceSearch(answerAfter = 150.milliseconds))
            .build(SearchPlacesUseCase.Args("karlst", null))
            .toList()

        assertEquals(listOf<SearchUpdate>(SearchUpdate.Found(FakePlaceSearch.Recorded)), updates)
    }

    @Test
    fun `trimmed text is searched near the given point`() = runTest {
        val fake = FakePlaceSearch()
        val near = GeoPoint(latitude = 50.08, longitude = 14.42)

        SearchPlacesUseCase(fake).build(SearchPlacesUseCase.Args(" karlst ", near)).toList()

        assertEquals(listOf(FakePlaceSearch.Search("karlst", near)), fake.searches)
    }

    @Test
    fun `an offline search fails the flow with the offline error`() = runTest {
        val flow = SearchPlacesUseCase(OfflinePlaceSearch()).build(SearchPlacesUseCase.Args("karlst", null))

        assertFailsWith<OfflineException> { flow.toList() }
    }

    /** Answers after [answerAfter], as a slow backend does. */
    private class SlowPlaceSearch(private val answerAfter: Duration) : PlaceSearch {
        override suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion> {
            delay(answerAfter)
            return FakePlaceSearch.Recorded
        }
    }

    private class OfflinePlaceSearch : PlaceSearch {
        override suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion> =
            throw OfflineException(cause = null)
    }
}
