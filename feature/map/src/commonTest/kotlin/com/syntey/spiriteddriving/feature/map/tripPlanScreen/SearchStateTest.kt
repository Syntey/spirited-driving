package com.syntey.spiriteddriving.feature.map.tripPlanScreen

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
}
