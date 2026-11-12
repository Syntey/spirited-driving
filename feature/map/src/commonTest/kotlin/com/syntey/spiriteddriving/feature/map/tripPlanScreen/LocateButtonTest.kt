package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlin.test.Test
import kotlin.test.assertEquals

class LocateButtonTest {

    @Test
    fun `precise access with the map following shows following`() {
        assertEquals(LocateButton.Following, LocateButton.of(LocationAccess.Precise, isFollowing = true))
    }

    @Test
    fun `precise access with the map not following shows not following`() {
        assertEquals(LocateButton.NotFollowing, LocateButton.of(LocationAccess.Precise, isFollowing = false))
    }

    @Test
    fun `approximate access with the map following shows following`() {
        assertEquals(LocateButton.Following, LocateButton.of(LocationAccess.Approximate, isFollowing = true))
    }

    @Test
    fun `approximate access with the map not following shows not following`() {
        assertEquals(LocateButton.NotFollowing, LocateButton.of(LocationAccess.Approximate, isFollowing = false))
    }

    @Test
    fun `denied access shows no access`() {
        assertEquals(LocateButton.NoAccess, LocateButton.of(LocationAccess.Denied, isFollowing = true))
    }

    @Test
    fun `denied for good shows no access`() {
        assertEquals(LocateButton.NoAccess, LocateButton.of(LocationAccess.DeniedForGood, isFollowing = false))
    }

    @Test
    fun `access not read yet shows no access`() {
        assertEquals(LocateButton.NoAccess, LocateButton.of(null, isFollowing = false))
    }
}
