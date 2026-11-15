// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.profile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.sheetFullScreenViewController
import com.syntey.spiriteddriving.core.ui.sheetScreenViewController
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreen
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreen
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreen
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreen
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenUi
import platform.UIKit.UIViewController

fun ProfileScreenUiController(screen: ProfileScreen): UIViewController = sheetFullScreenViewController {
    ProfileScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun SettingsScreenUiController(screen: SettingsScreen): UIViewController = sheetScreenViewController {
    SettingsScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun FeedbackScreenUiController(screen: FeedbackScreen): UIViewController = sheetScreenViewController {
    FeedbackScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun AboutScreenUiController(screen: AboutScreen): UIViewController = sheetScreenViewController {
    AboutScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun AccountScreenUiController(screen: AccountScreen): UIViewController = sheetScreenViewController {
    AccountScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun LicencesScreenUiController(screen: LicencesScreen): UIViewController = sheetScreenViewController {
    LicencesScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun LicenceTextScreenUiController(screen: LicenceTextScreen): UIViewController = sheetScreenViewController {
    LicenceTextScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}
