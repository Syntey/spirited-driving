package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * [distance] as the sheet shows them. Kilometres: "850 m" under 1 km in steps of 50 m, "4.2 km" under 10 km, then "28 km".
 * Miles: "0.3 mi" and "4.2 mi" under 10 mi, then "17 mi". [isApproximate], as with approximate location: whole kilometres or
 * miles, at least 1. The app's text is English, so the decimal point is a dot.
 */
internal fun distanceText(distance: Length, unit: DistanceUnit, isApproximate: Boolean): String = when (unit) {
    DistanceUnit.Kilometres -> kilometresText(distance, isApproximate)
    DistanceUnit.Miles -> milesText(distance, isApproximate)
}

private fun kilometresText(distance: Length, isApproximate: Boolean): String {
    // Decided on the rounded value, so 990 m reads as "1.0 km" and 9.96 km as "10 km".
    val metres = distance.inMetres
    val roundedMetres = max(STEP_METRES, (metres / STEP_METRES).roundToInt() * STEP_METRES)
    val tenthsOfKilometre = (metres * TENTHS_PER_UNIT / METRES_PER_KILOMETRE).roundToInt()
    return when {
        isApproximate -> "${max(1, distance.inKilometres.roundToInt())} km"
        roundedMetres < METRES_PER_KILOMETRE -> "$roundedMetres m"
        tenthsOfKilometre < TENTHS_LIMIT -> oneDecimal(tenthsOfKilometre, "km")
        else -> "${distance.inKilometres.roundToInt()} km"
    }
}

private fun milesText(distance: Length, isApproximate: Boolean): String {
    val miles = distance.inMiles
    val tenthsOfMile = max(1, (miles * TENTHS_PER_UNIT).roundToInt())
    return when {
        isApproximate -> "${max(1, miles.roundToInt())} mi"
        tenthsOfMile < TENTHS_LIMIT -> oneDecimal(tenthsOfMile, "mi")
        else -> "${miles.roundToInt()} mi"
    }
}

/** [tenths] as a number with one decimal, such as 42 as "4.2 km". */
private fun oneDecimal(tenths: Int, unit: String): String =
    "${tenths / TENTHS_PER_UNIT}.${tenths % TENTHS_PER_UNIT} $unit"

private const val METRES_PER_KILOMETRE = 1000.0
private const val STEP_METRES = 50

private const val TENTHS_PER_UNIT = 10

/** Up to this many tenths (10.0) the value shows one decimal; from it on, whole units. */
private const val TENTHS_LIMIT = 100
