package com.syntey.spiriteddriving.data.network.drive

import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import kotlin.test.Test
import kotlin.test.assertEquals

class RatingMappingTest {

    @Test
    fun `each reason maps to its code`() {
        val expected = mapOf(
            RatingReason.CurvyRoads to "CURVY_ROADS",
            RatingReason.NiceViews to "NICE_VIEWS",
            RatingReason.LittleTraffic to "LITTLE_TRAFFIC",
            RatingReason.SmoothSurface to "SMOOTH_SURFACE",
            RatingReason.WorthTheExtraTime to "WORTH_THE_EXTRA_TIME",
            RatingReason.BoringRoads to "BORING_ROADS",
            RatingReason.TooManyTowns to "TOO_MANY_TOWNS",
            RatingReason.TooNarrow to "TOO_NARROW",
            RatingReason.RoughSurface to "ROUGH_SURFACE",
            RatingReason.HeavyTraffic to "HEAVY_TRAFFIC",
            RatingReason.Roadworks to "ROADWORKS",
            RatingReason.TookTooLong to "TOOK_TOO_LONG",
            RatingReason.MapsAppWentAnotherWay to "MAPS_APP_WENT_ANOTHER_WAY",
        )

        val mapped = RatingReason.entries.associateWith { Rating(it.thumb, setOf(it)).toDto().reasons.single() }

        assertEquals(expected, mapped)
    }

    @Test
    fun `thumb down maps to DOWN`() {
        assertEquals("DOWN", Rating(Thumb.Down).toDto().thumb)
    }

    @Test
    fun `thumb up maps to UP`() {
        assertEquals("UP", Rating(Thumb.Up).toDto().thumb)
    }
}
