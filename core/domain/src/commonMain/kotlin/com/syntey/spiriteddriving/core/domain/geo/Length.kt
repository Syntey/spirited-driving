package com.syntey.spiriteddriving.core.domain.geo

import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

/** A length, such as a route's distance or a budget's extra distance. Built with [metres], [kilometres] or [miles]. */
@JvmInline
value class Length private constructor(private val metres: Double) : Comparable<Length> {

    /** The length in metres, unrounded. */
    val inMetres: Double get() = metres

    /** Rounded to whole metres, as the backend takes them. */
    val inWholeMetres: Int get() = metres.roundToInt()

    val inKilometres: Double get() = metres / METRES_PER_KILOMETRE

    val inMiles: Double get() = metres / METRES_PER_MILE

    val inFeet: Double get() = metres / METRES_PER_FOOT

    operator fun plus(other: Length): Length = Length(metres + other.metres)

    operator fun minus(other: Length): Length = Length(metres - other.metres)

    override fun compareTo(other: Length): Int = metres.compareTo(other.metres)

    companion object {
        val Int.metres: Length get() = Length(toDouble())
        val Double.metres: Length get() = Length(this)
        val Int.kilometres: Length get() = Length(this * METRES_PER_KILOMETRE)
        val Double.kilometres: Length get() = Length(this * METRES_PER_KILOMETRE)
        val Int.miles: Length get() = Length(this * METRES_PER_MILE)
        val Double.miles: Length get() = Length(this * METRES_PER_MILE)

        val Zero: Length = Length(0.0)
    }
}

private const val METRES_PER_KILOMETRE = 1000.0
private const val METRES_PER_MILE = 1609.344
private const val METRES_PER_FOOT = 0.3048
