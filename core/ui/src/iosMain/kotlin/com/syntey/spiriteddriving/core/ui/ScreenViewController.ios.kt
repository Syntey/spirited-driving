package com.syntey.spiriteddriving.core.ui

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
import com.syntey.spiriteddriving.core.ui.theme.MyApplicationTheme
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Hosts a screen's Compose content in native iOS navigation, which shows it in SwiftUI through `ComposeView`.
 *
 * Features call it from their `*ScreenUiController` functions; Swift calls those instead.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun screenViewController(content: @Composable () -> Unit): UIViewController = ComposeUIViewController(
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
