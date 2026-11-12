package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripPlanViewStateTest {

    @Test
    fun `the precise location note shows with approximate access`() {
        assertTrue(approximate(isPreciseLocationNoteClosed = false).showsPreciseLocationNote)
    }

    @Test
    fun `the precise location note hides with precise access`() {
        val state = approximate(isPreciseLocationNoteClosed = false).copy(locationAccess = LocationAccess.Precise)

        assertFalse(state.showsPreciseLocationNote)
    }

    @Test
    fun `the precise location note hides once closed`() {
        assertFalse(approximate(isPreciseLocationNoteClosed = true).showsPreciseLocationNote)
    }

    @Test
    fun `the precise location note hides while the user is outside the covered area`() {
        val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)
        val state = approximate(isPreciseLocationNoteClosed = false).let {
            it.copy(map = it.map.copy(userLocation = dresden, coveredArea = FakeCoverageSource.Czechia.area))
        }

        assertFalse(state.showsPreciseLocationNote)
    }

    @Test
    fun `a row shows the distance to a tenth with precise access`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = origin).suggestionRows.single()

        assertEquals("4.2 km", row.distance)
    }

    @Test
    fun `a row shows whole kilometres with approximate access`() {
        val row = stateWithSearch(access = LocationAccess.Approximate, from = origin).suggestionRows.single()

        assertEquals("4 km", row.distance)
    }

    @Test
    fun `a row has no distance without location access`() {
        val row = stateWithSearch(access = LocationAccess.Denied, from = origin).suggestionRows.single()

        assertNull(row.distance)
    }

    @Test
    fun `a row has no distance before the location is known`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = null).suggestionRows.single()

        assertNull(row.distance)
    }

    @Test
    fun `a row shows the distance in miles when the unit is miles`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = origin, unit = DistanceUnit.Miles).suggestionRows.single()

        assertEquals("2.6 mi", row.distance)
    }

    @Test
    fun `a row bolds the parts of the name that match the text`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = origin).suggestionRows.single()

        assertEquals(listOf(0..5), row.boldRanges)
    }

    @Test
    fun `rows keep the order of the suggestions`() {
        val state = stateWithSearch(access = LocationAccess.Precise, from = origin)
        val rows = state.copy(search = state.search.copy(suggestions = listOf(karlstejn, prague).toImmutableList())).suggestionRows

        assertEquals(listOf("Karlštejn", "Karlštejnská"), rows.map { it.suggestion.place.name })
    }

    private val origin = GeoPoint(latitude = 50.0, longitude = 14.0)

    // 4.2 km north of origin
    private val karlstejn = suggestion(name = "Karlštejn", location = GeoPoint(latitude = 50.03777151, longitude = 14.0))

    private val prague = suggestion(name = "Karlštejnská", location = GeoPoint(latitude = 50.0755, longitude = 14.4378))

    private fun stateWithSearch(access: LocationAccess?, from: GeoPoint?, unit: DistanceUnit = DistanceUnit.Kilometres) =
        TripPlanViewState(
            locationAccess = access,
            map = RouteMapState(userLocation = from),
            search = SearchState(text = "karlst", suggestions = listOf(karlstejn).toImmutableList()),
            distanceUnit = unit,
        )

    private fun suggestion(name: String, location: GeoPoint) =
        PlaceSuggestion(
            place = Place(name = name, area = null, location = location),
            isCovered = true,
            kind = PlaceKind.Town,
        )

    private fun approximate(isPreciseLocationNoteClosed: Boolean) = TripPlanViewState(
        locationAccess = LocationAccess.Approximate,
        isPreciseLocationNoteClosed = isPreciseLocationNoteClosed,
    )
}
