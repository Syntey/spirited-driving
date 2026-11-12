package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class SearchPlacesUseCaseTest {

    @Test
    fun `text shorter than two characters finds nothing without searching`() = runTest {
        val fake = FakePlaceSearch()

        val suggestions = SearchPlacesUseCase(fake).build(SearchPlacesUseCase.Args("k", null))

        assertTrue(suggestions.isEmpty())
        assertTrue(fake.searches.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search waits for a pause in typing before asking`() = runTest {
        val fake = FakePlaceSearch()
        val search = async { SearchPlacesUseCase(fake).build(SearchPlacesUseCase.Args("karlst", null)) }

        advanceTimeBy(299.milliseconds)
        assertTrue(fake.searches.isEmpty())

        advanceTimeBy(2.milliseconds)
        assertEquals(1, fake.searches.size)
        search.await()
    }

    @Test
    fun `trimmed text is searched near the given point`() = runTest {
        val fake = FakePlaceSearch()
        val near = GeoPoint(latitude = 50.08, longitude = 14.42)

        SearchPlacesUseCase(fake).build(SearchPlacesUseCase.Args(" karlst ", near))

        assertEquals(listOf(FakePlaceSearch.Search("karlst", near)), fake.searches)
    }
}
