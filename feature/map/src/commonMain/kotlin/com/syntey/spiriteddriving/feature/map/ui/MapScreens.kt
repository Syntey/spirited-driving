package com.syntey.spiriteddriving.feature.map.ui

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetComponentFactory
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object MapScreens {

    fun mapSheet(componentContext: AppComponentContext, navigation: MapSheetScreenNavigation): MapSheetScreen =
        MapSheetComponentFactory.createComponent(componentContext, navigation)
}
