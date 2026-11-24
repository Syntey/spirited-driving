package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning

import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import kotlinx.coroutines.flow.StateFlow

/**
 * Route planning in the map sheet, with the start search it opens over itself: its own state and taps, which the sheet's UI
 * uses directly. The start search's field and results still go through the sheet's own actions.
 */
interface RoutePlanningScreen {
    val viewState: StateFlow<RoutePlanningViewState>
    val actions: Actions

    interface Actions {
        /** Tapped a route in the list. */
        fun onRouteClick(routeId: String)

        /** Tapped a budget chip. */
        fun onBudgetClick(budget: Budget)

        /** Switched the budget's unit. */
        fun onBudgetKindChange(kind: BudgetKind)

        /** Tapped Try again after planning failed. */
        fun onRetryClick()

        /** Tapped "From" in the header. */
        fun onStartClick()

        /** Tapped Cancel. */
        fun onCloseClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onRouteClick(routeId: String) = Unit
                override fun onBudgetClick(budget: Budget) = Unit
                override fun onBudgetKindChange(kind: BudgetKind) = Unit
                override fun onRetryClick() = Unit
                override fun onStartClick() = Unit
                override fun onCloseClick() = Unit
            }
        }
    }
}
