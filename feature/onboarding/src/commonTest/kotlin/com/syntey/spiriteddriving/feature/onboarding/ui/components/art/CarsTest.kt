package com.syntey.spiriteddriving.feature.onboarding.ui.components.art

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarsTest {

    // Where tools/first-launch-art/generate.mjs puts the cars on each page, as it prints them: x from the page's left edge
    private val designed = listOf(
        Designed(lead = 226.9f to 221.4f, follower = 166.8f to 258.4f),
        Designed(lead = 224.3f to 331.8f, follower = 172f to 389.6f),
        Designed(lead = 256.3f to 285.9f, follower = 283.3f to 235.6f),
        Designed(lead = 240.9f to 261.6f, follower = 177.8f to 227f),
        Designed(lead = 227.4f to 271.9f, follower = 168.9f to 314.8f),
    )

    @Test
    fun `on each page the cars sit where the design puts them`() {
        designed.forEachIndexed { page, design ->
            val pageLeft = ArtRoad.PAGE_WIDTH * page
            assertNear(design.lead, Cars.Default.leadAt(page.toFloat()), pageLeft, "lead car on page ${page + 1}")
            assertNear(design.follower, Cars.Default.followerAt(page.toFloat()), pageLeft, "following car on page ${page + 1}")
        }
    }

    @Test
    fun `halfway between two pages the lead car is between its places on them`() {
        val first = Cars.Default.leadAt(0f)
        val halfway = Cars.Default.leadAt(0.5f)
        val second = Cars.Default.leadAt(1f)

        assertTrue(halfway.x > first.x && halfway.x < second.x, "x ${halfway.x} between ${first.x} and ${second.x}")
    }

    @Test
    fun `pulled past the last page the cars keep driving`() {
        val last = ArtRoad.PAGE_COUNT - 1f

        assertTrue(Cars.Default.leadAt(last + 0.2f).x > Cars.Default.leadAt(last).x)
    }

    private fun assertNear(expected: Pair<Float, Float>, actual: Placement, pageLeft: Float, what: String) {
        assertEquals(expected.first, actual.x - pageLeft, TOLERANCE, "x of the $what")
        assertEquals(expected.second, actual.y, TOLERANCE, "y of the $what")
    }

    private data class Designed(val lead: Pair<Float, Float>, val follower: Pair<Float, Float>)

    private companion object {
        // The generator prints one decimal and samples the road unrounded; the app reads it rounded to one decimal.
        const val TOLERANCE = 0.3f
    }
}
