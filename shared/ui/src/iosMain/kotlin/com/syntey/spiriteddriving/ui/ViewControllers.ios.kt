// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.syntey.spiriteddriving.feature.ui.firstScreen.FirstScreen
import com.syntey.spiriteddriving.feature.ui.loginScreen.LoginScreen
import com.syntey.spiriteddriving.feature.ui.picker.PickerScreen
import com.syntey.spiriteddriving.feature.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.ui.secondScreen.SecondScreen
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreen
import com.syntey.spiriteddriving.ui.screen.FirstScreenUi
import com.syntey.spiriteddriving.ui.screen.LoginScreenUi
import com.syntey.spiriteddriving.ui.screen.PickerScreenUi
import com.syntey.spiriteddriving.ui.screen.ProfileScreenUi
import com.syntey.spiriteddriving.ui.screen.SecondScreenUi
import com.syntey.spiriteddriving.ui.screen.ThirdScreenUi
import com.syntey.spiriteddriving.ui.theme.MyApplicationTheme
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

@OptIn(ExperimentalComposeUiApi::class)
private fun screenViewController(content: @Composable () -> Unit): UIViewController = ComposeUIViewController(
    configure = {
        // Keyboard is part of the safe drawing insets applied below, panning the content on top of that would move it twice.
        onFocusBehavior = OnFocusBehavior.DoNothing

        // Render-thread frames hand their drawable back through the main queue, which a pushed screen's first synchronous
        // draw is blocking while it waits for one, so the native push would stall until the drawable acquisition times out.
        parallelRendering = false
    },
) {
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // SwiftUI lays the screen out under native bars (see `composeScreen()` in Swift), so that the background reaches
            // behind them. The bars, together with the keyboard, are then kept clear of content here.
            Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                content()
            }
        }
    }
}
