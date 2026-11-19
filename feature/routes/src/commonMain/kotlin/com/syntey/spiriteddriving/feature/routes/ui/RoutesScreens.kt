package com.syntey.spiriteddriving.feature.routes.ui

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesComponentFactory
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreenNavigation
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryComponentFactory
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object RoutesScreens {

    fun savedRoutes(componentContext: AppComponentContext, navigation: SavedRoutesScreenNavigation): SavedRoutesScreen =
        SavedRoutesComponentFactory.createComponent(componentContext, navigation)

    fun tripHistory(componentContext: AppComponentContext, navigation: TripHistoryScreenNavigation): TripHistoryScreen =
        TripHistoryComponentFactory.createComponent(componentContext, navigation)
}
