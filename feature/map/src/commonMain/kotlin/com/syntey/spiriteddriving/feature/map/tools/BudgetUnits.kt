package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.map.tools.Constants.Budgets
import kotlin.math.abs

/** The distance chips of [unit]: kilometres and miles each have chips of their own. */
internal fun distanceChips(unit: DistanceUnit): List<Budget.Distance> = when (unit) {
    DistanceUnit.Kilometres -> Budgets.DISTANCE_KILOMETRES
    DistanceUnit.Miles -> Budgets.DISTANCE_MILES
}

/**
 * This choice with its distance moved to the chip of [unit] nearest to it, the smaller chip on a tie, so a kept distance
 * keeps its size when the unit changes. Time and kind are kept. A distance already on a chip is kept as it is.
 */
internal fun BudgetChoice.inUnit(unit: DistanceUnit): BudgetChoice {
    val chips = distanceChips(unit)
    if (distance in chips) return this
    val nearest = chips.minWith(
        compareBy<Budget.Distance>({ abs(it.extra.inMetres - distance.extra.inMetres) }, { it.extra.inMetres }),
    )
    return copy(distance = nearest)
}
