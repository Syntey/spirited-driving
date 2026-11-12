package com.syntey.spiriteddriving.feature.map

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanComponentFactory
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object MapScreens {

    fun tripPlan(componentContext: AppComponentContext, navigation: TripPlanScreenNavigation): TripPlanScreen =
        TripPlanComponentFactory.createComponent(componentContext, navigation)
}
