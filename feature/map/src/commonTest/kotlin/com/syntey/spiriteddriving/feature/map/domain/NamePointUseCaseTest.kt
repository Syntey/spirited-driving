package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.Place
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NamePointUseCaseTest {

    private val karlstejn = GeoPoint(latitude = 49.9394, longitude = 14.1882)

    @Test
    fun `the named place near the point is returned`() = runTest {
        val named = FakePlaceSearch.RecordedPlaceAt

        val result = NamePointUseCase(FakePlaceSearch(placeAt = named)).build(karlstejn)

        assertEquals(named, result)
    }

    @Test
    fun `the lookup is asked for the point`() = runTest {
        val placeSearch = FakePlaceSearch()

        NamePointUseCase(placeSearch).build(karlstejn)

        assertEquals(listOf(karlstejn), placeSearch.lookups)
    }

    @Test
    fun `no place near the point gives an unnamed place at the point`() = runTest {
        val result = NamePointUseCase(FakePlaceSearch(placeAt = null)).build(karlstejn)

        assertEquals(Place(name = null, area = "49.9394, 14.1882", location = karlstejn), result)
    }

    @Test
    fun `a negative coordinate keeps its sign`() {
        val place = unnamedPlace(GeoPoint(latitude = 45.0, longitude = -30.0))

        assertEquals("45.0000, -30.0000", place.area)
    }

    @Test
    fun `coordinates are rounded to four decimals`() {
        val place = unnamedPlace(GeoPoint(latitude = 49.93945678, longitude = 14.1882))

        assertEquals("49.9395, 14.1882", place.area)
    }

    @Test
    fun `a tiny negative coordinate has no minus sign`() {
        val place = unnamedPlace(GeoPoint(latitude = -0.00001, longitude = 0.0))

        assertEquals("0.0000, 0.0000", place.area)
    }
}
