// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.map

import com.syntey.spiriteddriving.core.ui.sheetViewController
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanSheetUi
import platform.UIKit.UIViewController

/**
 * The map sheet's content. [onHeightChanged] gets the collapsed sheet's height in points, which the sheet's collapsed detent
 * follows.
 */
fun TripPlanSheetUiController(screen: TripPlanScreen, onHeightChanged: (Double) -> Unit): UIViewController = sheetViewController {
    TripPlanSheetUi(screen = screen, onCollapsedHeightChange = { height -> onHeightChanged(height.value.toDouble()) })
}
