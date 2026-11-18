// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.routes.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Constraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.ui.sheetScreenViewController
import com.syntey.spiriteddriving.core.ui.sheetViewController
import com.syntey.spiriteddriving.feature.routes.model.ui.RatingSheet
import com.syntey.spiriteddriving.feature.routes.ui.components.RatingSheetContent
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreenUi
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreenUi
import platform.UIKit.UIViewController

fun SavedRoutesScreenUiController(screen: SavedRoutesScreen): UIViewController = sheetScreenViewController {
    SavedRoutesScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun TripHistoryScreenUiController(screen: TripHistoryScreen): UIViewController = sheetScreenViewController {
    TripHistoryScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

/**
 * The rating sheet's content on the native sheet over the list. [onHeightChanged] gets the content's height in points, which
 * the sheet's detent follows, so the sheet fits what it shows.
 */
fun RatingSheetUiController(screen: TripHistoryScreen, onHeightChanged: (Double) -> Unit): UIViewController = sheetViewController {
    RatingSheetIosUi(screen = screen, onHeightChanged = onHeightChanged)
}

@Composable
private fun RatingSheetIosUi(screen: TripHistoryScreen, onHeightChanged: (Double) -> Unit) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    // Kept while the native sheet slides away, so the content doesn't go blank before it's gone
    val shown = remember { mutableStateOf<RatingSheet?>(null) }
    viewState.ratingSheet?.let { shown.value = it }
    val sheet = shown.value ?: return
    val density = LocalDensity.current
    val reportHeight by rememberUpdatedState(onHeightChanged)
    val contentHeight = remember { HeightHolder() }
    LaunchedEffect(contentHeight.height) { contentHeight.height?.let { reportHeight(it.value.toDouble()) } }
    RatingSheetContent(
        sheet = sheet,
        actions = screen.actions,
        modifier = Modifier.measureOnly { height -> contentHeight.height = with(density) { height.toDp() } },
    )
}

private class HeightHolder {
    var height by mutableStateOf<Dp?>(null)
}

// The content's height, measured unplaced so it's the height the content needs, whatever the sheet's height is
private fun Modifier.measureOnly(onHeight: (Int) -> Unit): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    onHeight(placeable.height)
    layout(0, 0) {}
}
