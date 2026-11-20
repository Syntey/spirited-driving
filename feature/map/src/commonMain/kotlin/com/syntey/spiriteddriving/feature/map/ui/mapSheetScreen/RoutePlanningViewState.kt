package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState

/**
 * What route planning shows.
 *
 * @property planning The trip: its start, destination and budget, the plan and the selected route.
 * @property budgetChoice The budget planning starts with, and the last value in each unit.
 * @property distanceUnit The unit distances are shown in.
 */
internal data class RoutePlanningViewState(
    val planning: RoutePlanningState,
    val budgetChoice: BudgetChoice,
    val distanceUnit: DistanceUnit,
)
