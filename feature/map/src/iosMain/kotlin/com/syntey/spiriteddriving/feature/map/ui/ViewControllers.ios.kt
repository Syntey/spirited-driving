// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.map.ui

import com.syntey.spiriteddriving.core.ui.sheetViewController
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetUi
import platform.UIKit.UIViewController

/**
 * The map sheet's content. [onHeightChanged] gets the collapsed sheet's height in points, which the sheet's collapsed detent
 * follows.
 */
fun MapSheetUiController(screen: MapSheetScreen, onHeightChanged: (Double) -> Unit): UIViewController = sheetViewController {
    MapSheetUi(screen = screen, onCollapsedHeightChange = { height -> onHeightChanged(height.value.toDouble()) })
}
