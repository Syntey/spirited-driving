// The numbers are the coefficients of a cubic Bézier curve and its derivative, and degrees in a half turn
@file:Suppress("MagicNumber")

package com.syntey.spiriteddriving.feature.onboarding.art

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Where something sits on the panorama, and which way it faces: 0° is right, 90° down.
 */
internal data class Placement(val x: Float, val y: Float, val degrees: Float)

/**
 * The road through the panorama, from [ArtRoad.segments], measured along its length so the cars can drive along it.
 * Each cubic segment is sampled at the same points as in the generator, so the cars sit where the design shows them.
 */
internal class Road(segments: FloatArray) {

    private val xs: FloatArray
    private val ys: FloatArray
    private val angles: FloatArray
    private val distances: FloatArray

    init {
        val segmentCount = segments.size / NUMBERS_PER_SEGMENT
        val count = segmentCount * (SAMPLES_PER_SEGMENT + 1)
        xs = FloatArray(count)
        ys = FloatArray(count)
        angles = FloatArray(count)
        distances = FloatArray(count)
        var i = 0
        for (segment in 0 until segmentCount) {
            val s = segment * NUMBERS_PER_SEGMENT
            for (k in 0..SAMPLES_PER_SEGMENT) {
                val t = k.toFloat() / SAMPLES_PER_SEGMENT
                xs[i] = bezier(segments[s], segments[s + 2], segments[s + 4], segments[s + 6], t)
                ys[i] = bezier(segments[s + 1], segments[s + 3], segments[s + 5], segments[s + 7], t)
                angles[i] = atan2(
                    tangent(segments[s + 1], segments[s + 3], segments[s + 5], segments[s + 7], t),
                    tangent(segments[s], segments[s + 2], segments[s + 4], segments[s + 6], t),
                )
                distances[i] = if (i == 0) 0f else distances[i - 1] + hypot(xs[i] - xs[i - 1], ys[i] - ys[i - 1])
                i++
            }
        }
    }

    /**
     * The car whose centre is [distance] along the road, in the lane [lane] right of the centre line.
     */
    fun carAt(distance: Float, lane: Float): Placement {
        val i = sampleAt(distance)
        val angle = angles[i]
        return Placement(
            x = xs[i] - sin(angle) * lane,
            y = ys[i] + cos(angle) * lane,
            degrees = (angle * 180 / PI).toFloat(),
        )
    }

    // The first sample at or past `distance`, as in the generator.
    private fun sampleAt(distance: Float): Int {
        if (distance <= 0f) return 0
        var low = 0
        var high = distances.lastIndex
        while (high - low > 1) {
            val middle = (low + high) / 2
            if (distances[middle] < distance) low = middle else high = middle
        }
        return high
    }

    private companion object {
        const val NUMBERS_PER_SEGMENT = 8
        const val SAMPLES_PER_SEGMENT = 400

        fun bezier(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
            val u = 1 - t
            return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3
        }

        fun tangent(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
            val u = 1 - t
            return 3 * u * u * (p1 - p0) + 6 * u * t * (p2 - p1) + 3 * t * t * (p3 - p2)
        }
    }
}

/**
 * The two cars at [position] through the pages: 0 is the first page, 1.5 halfway between the second and the third. They
 * drive as the pages move, and keep going a little when a page is pulled past the first or the last.
 */
internal class Cars(private val road: Road, private val leads: FloatArray, private val gap: Float, private val lane: Float) {

    /** The car in front, after the MX-5. */
    fun leadAt(position: Float): Placement = road.carAt(leadDistance(position), lane)

    /** The car following it, after the GR86. */
    fun followerAt(position: Float): Placement = road.carAt(leadDistance(position) - gap, lane)

    private fun leadDistance(position: Float): Float {
        val last = leads.lastIndex
        return when {
            position <= 0f -> leads[0] + (leads[1] - leads[0]) * position

            position >= last -> leads[last] + (leads[last] - leads[last - 1]) * (position - last)

            else -> {
                val page = floor(position).toInt()
                leads[page] + (leads[page + 1] - leads[page]) * (position - page)
            }
        }
    }

    companion object {
        val Default by lazy { Cars(Road(ArtRoad.segments), ArtRoad.leads, ArtRoad.GAP, ArtRoad.LANE) }
    }
}
