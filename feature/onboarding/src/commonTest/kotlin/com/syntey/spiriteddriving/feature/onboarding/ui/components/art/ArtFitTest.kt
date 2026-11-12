package com.syntey.spiriteddriving.feature.onboarding.ui.components.art

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtFitTest {

    @Test
    fun `with room above the panel the panorama fills the height`() {
        val fit = ArtFit.of(height = 844f, clearTop = 120f, clearBottom = 460f)

        assertEquals(ArtFit(unit = 1f, shiftUp = 0f), fit)
    }

    @Test
    fun `a taller panel moves the panorama up until the cars clear it`() {
        val fit = ArtFit.of(height = 844f, clearTop = 120f, clearBottom = 400f)

        assertEquals(1f, fit.unit, TOLERANCE)
        assertEquals(400f, carsBottom(fit), TOLERANCE)
    }

    @Test
    fun `when moving up would take the cars under the controls the panorama shrinks to fit between them`() {
        val fit = ArtFit.of(height = 844f, clearTop = 80f, clearBottom = 300f)

        assertTrue(fit.unit < 1f, "unit ${fit.unit}")
        assertEquals(80f, carsTop(fit), TOLERANCE)
        assertEquals(300f, carsBottom(fit), TOLERANCE)
    }

    @Test
    fun `with almost no room the panorama shrinks no further than half`() {
        val fit = ArtFit.of(height = 844f, clearTop = 80f, clearBottom = 100f)

        assertEquals(ArtFit.MIN_SCALE, fit.unit, TOLERANCE)
    }

    @Test
    fun `the cars stay between their top and bottom on every page and when pulled past the ends`() {
        // The rubber band never pulls more than two thirds of a page past either end.
        val last = (ArtRoad.PAGE_COUNT - 1) * STEPS_PER_PAGE
        for (step in -OVERSCROLL_STEPS..last + OVERSCROLL_STEPS) {
            val position = step.toFloat() / STEPS_PER_PAGE
            for (car in listOf(Cars.Default.leadAt(position), Cars.Default.followerAt(position))) {
                assertTrue(car.y in ArtFit.CARS_TOP..ArtFit.CARS_BOTTOM, "car at y ${car.y} at position $position")
            }
        }
    }

    private fun carsTop(fit: ArtFit) = (ArtFit.CARS_TOP - ArtFit.CAR_REACH) * fit.unit - fit.shiftUp

    private fun carsBottom(fit: ArtFit) = (ArtFit.CARS_BOTTOM + ArtFit.CAR_REACH) * fit.unit - fit.shiftUp

    private companion object {
        const val TOLERANCE = 0.01f
        const val STEPS_PER_PAGE = 100
        const val OVERSCROLL_STEPS = 67
    }
}
