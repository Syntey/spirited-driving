package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `denied hides the dot, stops following and forgets the location`() {
        val state = RouteMapState(
            showsUserLocation = true,
            followsUser = true,
            userLocation = GeoPoint(latitude = 50.08, longitude = 14.42),
        )

        assertEquals(RouteMapState(), state.withAccess(LocationAccess.Denied))
    }
}
