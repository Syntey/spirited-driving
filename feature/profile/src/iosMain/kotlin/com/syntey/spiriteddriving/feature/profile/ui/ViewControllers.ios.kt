// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.profile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.sheetScreenViewController
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenUi
import platform.UIKit.UIViewController

fun ProfileScreenUiController(screen: ProfileScreen): UIViewController = sheetScreenViewController {
    ProfileScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun SettingsScreenUiController(screen: SettingsScreen): UIViewController = sheetScreenViewController {
    SettingsScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}
