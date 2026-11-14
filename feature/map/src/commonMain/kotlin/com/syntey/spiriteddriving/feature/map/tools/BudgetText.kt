package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.math.roundToInt

/**
 * [budget] as its chip shows it, with the sign: "+45 min" for 45 minutes, "+1.5 h" for 90 minutes, "+2 h" for two
 * hours, and "+50 km" for 50 kilometres. Hours take one decimal when they aren't whole, in English, so the decimal point
 * is always a dot.
 */
internal fun budgetText(budget: Budget): StringDesc = when (budget) {
    is Budget.Time -> {
        val minutes = budget.extra.inWholeMinutes.toInt()
        if (minutes < MINUTES_PER_HOUR) {
            StringDesc.ResourceFormatted(MR.strings.budget_minutes, minutes)
        } else {
            StringDesc.ResourceFormatted(MR.strings.budget_hours, hoursText(minutes))
        }
    }

    is Budget.Distance -> StringDesc.ResourceFormatted(MR.strings.budget_kilometres, budget.extra.inKilometres.roundToInt())
}

// One decimal from the minutes: 90 is "1.5", 120 is "2"
private fun hoursText(minutes: Int): String {
    val hours = minutes / MINUTES_PER_HOUR
    val tenthsOfHour = minutes % MINUTES_PER_HOUR * TENTHS / MINUTES_PER_HOUR
    return if (tenthsOfHour == 0) "$hours" else "$hours.$tenthsOfHour"
}

private const val MINUTES_PER_HOUR = 60
private const val TENTHS = 10
