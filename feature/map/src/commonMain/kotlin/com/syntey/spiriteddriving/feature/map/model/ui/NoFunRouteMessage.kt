package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.route.Budget
import dev.icerock.moko.resources.desc.StringDesc

/**
 * The message above the fastest route when no fun route fits the budget.
 *
 * @property useBudget The smallest chip that fits the closest fun route, which the button picks; null when no chip is big
 *   enough, and the button is left out.
 */
data class NoFunRouteMessage(val text: StringDesc, val useBudget: UseBudgetButton?)

/** The "Use +30 min" button: the [budget] it picks, and its [label]. */
data class UseBudgetButton(val budget: Budget, val label: StringDesc)
