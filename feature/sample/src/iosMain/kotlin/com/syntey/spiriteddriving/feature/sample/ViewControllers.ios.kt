// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.screenViewController
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreen
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreenUi
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreen
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreenUi
import com.syntey.spiriteddriving.feature.sample.picker.PickerScreen
import com.syntey.spiriteddriving.feature.sample.picker.PickerScreenUi
import com.syntey.spiriteddriving.feature.sample.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.sample.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreen
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreenUi
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreen
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreenUi
import platform.UIKit.UIViewController

/*
 * Navigation on iOS stays native (SwiftUI `NavigationStack`, `TabView` and sheets), and each screen is hosted in it as
 * a Compose view controller created by one of the functions below.
 */

fun LoginScreenUiController(screen: LoginScreen): UIViewController = screenViewController {
    LoginScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun FirstScreenUiController(screen: FirstScreen): UIViewController = screenViewController {
    FirstScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun SecondScreenUiController(screen: SecondScreen): UIViewController = screenViewController {
    SecondScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun ThirdScreenUiController(screen: ThirdScreen): UIViewController = screenViewController {
    ThirdScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun ProfileScreenUiController(screen: ProfileScreen): UIViewController = screenViewController {
    ProfileScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}

fun PickerScreenUiController(screen: PickerScreen): UIViewController = screenViewController {
    PickerScreenUi(pickerScreen = screen, modifier = Modifier.fillMaxSize())
}
