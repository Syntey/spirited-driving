// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.routes.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.sheetScreenViewController
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreenUi
import platform.UIKit.UIViewController

fun SavedRoutesScreenUiController(screen: SavedRoutesScreen): UIViewController = sheetScreenViewController {
    SavedRoutesScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}
