package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteMapStateTest {

    @Test
    fun `turning access on without access starts following and shows the dot`() {
        assertEquals(
            RouteMapState(showsUserLocation = true, followsUser = true),
            RouteMapState().withAccess(LocationAccess.Precise),
        )
    }

    @Test
    fun `precise access again while following stays following`() {
        val state = RouteMapState(showsUserLocation = true, followsUser = true)

        assertEquals(true, state.withAccess(LocationAccess.Precise).followsUser)
    }

    @Test
    fun `precise access again while not following stays not following`() {
        val state = RouteMapState(showsUserLocation = true, followsUser = false)

        assertEquals(false, state.withAccess(LocationAccess.Precise).followsUser)
    }

    @Test
    fun `approximate access counts as access`() {
        assertEquals(
            RouteMapState(showsUserLocation = true, followsUser = true),
            RouteMapState().withAccess(LocationAccess.Approximate),
        )
    }

    @Test
    fun `denied hides the dot and stops following and forgets the location`() {
        val state = RouteMapState(
            showsUserLocation = true,
            followsUser = true,
            userLocation = GeoPoint(latitude = 50.08, longitude = 14.42),
        )

        assertEquals(RouteMapState(), state.withAccess(LocationAccess.Denied))
    }

    private val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)
    private val prague = GeoPoint(latitude = 50.08, longitude = 14.42)
    private val covered = FakeCoverageSource.Czechia
    private val following = RouteMapState(showsUserLocation = true, followsUser = true)

    @Test
    fun `a user outside stops following and the overview includes both`() {
        val state = following.withCoverage(covered).withUserLocation(dresden)

        assertEquals(false, state.followsUser)
        assertEquals(covered.bounds.including(dresden), state.overview)
        assertEquals(1, state.overviewRequest)
    }

    @Test
    fun `a location known before the covered area also stops following and includes both`() {
        val state = following.withUserLocation(dresden).withCoverage(covered)

        assertEquals(false, state.followsUser)
        assertEquals(covered.bounds.including(dresden), state.overview)
        assertEquals(1, state.overviewRequest)
    }

    @Test
    fun `a user inside keeps following and the overview is the covered area`() {
        val state = following.withCoverage(covered).withUserLocation(prague)

        assertEquals(true, state.followsUser)
        assertEquals(covered.bounds, state.overview)
        assertEquals(0, state.overviewRequest)
    }

    @Test
    fun `a user just outside the border requests the overview even when the overview is unchanged`() {
        val justOutside = GeoPoint(latitude = 49.5, longitude = 12.2)
        val state = following.withCoverage(covered).withUserLocation(justOutside)

        assertEquals(covered.bounds, state.overview)
        assertEquals(1, state.overviewRequest)
    }

    @Test
    fun `a later location outside does not change the overview again`() {
        val first = following.withCoverage(covered).withUserLocation(dresden)
        val later = first.withUserLocation(GeoPoint(latitude = 52.0, longitude = 14.0))

        assertEquals(first.overview, later.overview)
        assertEquals(first.overviewRequest, later.overviewRequest)
    }

    @Test
    fun `a later covered area keeps the overview`() {
        val first = following.withCoverage(covered).withUserLocation(dresden)
        val later = first.withCoverage(covered)

        assertEquals(first.overview, later.overview)
    }

    @Test
    fun `a destination drops its pin and stops following`() {
        val state = following.withDestination(prague)

        assertEquals(prague, state.destination)
        assertEquals(false, state.followsUser)
    }

    @Test
    fun `a picked point drops its pin and stops following but keeps the camera`() {
        val before = following.withCoverage(covered).withUserLocation(prague)
        val state = before.withPickedPoint(dresden)

        assertEquals(dresden, state.destination)
        assertEquals(false, state.followsUser)
        assertEquals(before.overview, state.overview)
        assertEquals(before.overviewRequest, state.overviewRequest)
    }

    @Test
    fun `a destination moves the map around its pin`() {
        val before = following.withCoverage(covered)
        val state = before.withDestination(prague)

        assertTrue(state.overview.southWest.latitude < prague.latitude)
        assertTrue(state.overview.southWest.longitude < prague.longitude)
        assertTrue(state.overview.northEast.latitude > prague.latitude)
        assertTrue(state.overview.northEast.longitude > prague.longitude)
        assertEquals(before.overviewRequest + 1, state.overviewRequest)
    }

    @Test
    fun `outside is false until both the covered area and the location are known`() {
        assertFalse(following.withCoverage(covered).isOutsideCoveredArea)
        assertFalse(following.withUserLocation(dresden).isOutsideCoveredArea)
        assertTrue(following.withCoverage(covered).withUserLocation(dresden).isOutsideCoveredArea)
    }

    @Test
    fun `every point is covered until the covered area is loaded`() {
        val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)

        assertTrue(RouteMapState().covers(dresden))
    }

    @Test
    fun `a point outside the loaded covered area is not covered`() {
        val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)

        assertFalse(RouteMapState(coveredArea = FakeCoverageSource.Czechia.area).covers(dresden))
    }
}
