package com.syntey.spiriteddriving.core.resources.measure

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * [distance] as the sheet shows them. Kilometres: "850 m" under 1 km in steps of 50 m, "4.2 km" under 10 km, then "28 km".
 * Miles: "0.3 mi" and "4.2 mi" under 10 mi, then "17 mi". [isApproximate], as with approximate location: whole kilometres or
 * miles, at least 1. The app's English text, so the decimal point is a dot.
 */
fun distanceText(distance: Length, unit: DistanceUnit, isApproximate: Boolean): StringDesc = when (unit) {
    DistanceUnit.Kilometres -> kilometresText(distance, isApproximate)
    DistanceUnit.Miles -> milesText(distance, isApproximate)
}

private fun kilometresText(distance: Length, isApproximate: Boolean): StringDesc {
    // Decided on the rounded value, so 990 m reads as "1.0 km" and 9.96 km as "10 km".
    val metres = distance.inMetres
    val roundedMetres = max(STEP_METRES, (metres / STEP_METRES).roundToInt() * STEP_METRES)
    val tenthsOfKilometre = (metres * TENTHS_PER_UNIT / METRES_PER_KILOMETRE).roundToInt()
    return when {
        isApproximate -> kilometresDesc(max(1, distance.inKilometres.roundToInt()).toString())
        roundedMetres < METRES_PER_KILOMETRE -> StringDesc.ResourceFormatted(MR.strings.unit_metres, roundedMetres.toString())
        tenthsOfKilometre < TENTHS_LIMIT -> kilometresDesc(oneDecimal(tenthsOfKilometre))
        else -> kilometresDesc(distance.inKilometres.roundToInt().toString())
    }
}

private fun milesText(distance: Length, isApproximate: Boolean): StringDesc {
    val miles = distance.inMiles
    val tenthsOfMile = max(1, (miles * TENTHS_PER_UNIT).roundToInt())
    return when {
        isApproximate -> StringDesc.ResourceFormatted(MR.strings.unit_miles, max(1, miles.roundToInt()).toString())
        tenthsOfMile < TENTHS_LIMIT -> StringDesc.ResourceFormatted(MR.strings.unit_miles, oneDecimal(tenthsOfMile))
        else -> StringDesc.ResourceFormatted(MR.strings.unit_miles, miles.roundToInt().toString())
    }
}

/**
 * [height] as the sheet shows a climb: whole metres or feet, rounded to 10, such as "810 m" or "2,660 ft". Thousands get a
 * comma.
 */
fun heightText(height: Length, unit: DistanceUnit): StringDesc = when (unit) {
    DistanceUnit.Kilometres -> StringDesc.ResourceFormatted(MR.strings.unit_metres, roundedToTen(height.inMetres).withThousands())
    DistanceUnit.Miles -> StringDesc.ResourceFormatted(MR.strings.unit_feet, roundedToTen(height.inFeet).withThousands())
}

private fun kilometresDesc(number: String): StringDesc = StringDesc.ResourceFormatted(MR.strings.unit_kilometres, number)

private fun roundedToTen(value: Double): Int = (value / HEIGHT_STEP).roundToInt() * HEIGHT_STEP

// 1234 as "1,234"; the app's English text groups thousands with commas
private fun Int.withThousands(): String = toString().reversed().chunked(DIGITS_PER_GROUP).joinToString(",").reversed()

private const val HEIGHT_STEP = 10
private const val DIGITS_PER_GROUP = 3

/** [tenths] as a number with one decimal, such as 42 as "4.2". */
private fun oneDecimal(tenths: Int): String = "${tenths / TENTHS_PER_UNIT}.${tenths % TENTHS_PER_UNIT}"

private const val METRES_PER_KILOMETRE = 1000.0
private const val STEP_METRES = 50

private const val TENTHS_PER_UNIT = 10

/** Up to this many tenths (10.0) the value shows one decimal; from it on, whole units. */
private const val TENTHS_LIMIT = 100
