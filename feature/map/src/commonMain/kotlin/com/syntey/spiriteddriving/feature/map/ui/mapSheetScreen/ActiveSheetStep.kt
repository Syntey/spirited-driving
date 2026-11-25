package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningScreen

/**
 * The kind of the sheet step on top of the stack, which decides the sheet's content: the destination search, route planning with
 * its start search, a saved route's or a past drive's one route, or the rating step.
 */
sealed interface ActiveSheetStep {
    data object Search : ActiveSheetStep
    data class RoutePlanning(val screen: RoutePlanningScreen) : ActiveSheetStep
    data class OneRoute(val screen: OneRouteScreen) : ActiveSheetStep
    data class Rating(val screen: RatingScreen) : ActiveSheetStep
}
