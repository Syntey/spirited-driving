// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.map

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.syntey.spiriteddriving.core.ui.sheetViewController
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanSheetUi
import platform.UIKit.UIViewController

/**
 * The map sheet's content. [onHeightChanged] gets its height in points, which the sheet's collapsed detent follows.
 */
fun TripPlanSheetUiController(screen: TripPlanScreen, onHeightChanged: (Double) -> Unit): UIViewController = sheetViewController {
    val density = LocalDensity.current
    TripPlanSheetUi(
        screen = screen,
        // Unbounded, so the content reports the height it needs even while the sheet is still shorter
        modifier = Modifier
            .wrapContentHeight(align = Alignment.Top, unbounded = true)
            .onSizeChanged { size -> onHeightChanged(with(density) { size.height.toFloat().toDp().value.toDouble() }) },
    )
}
