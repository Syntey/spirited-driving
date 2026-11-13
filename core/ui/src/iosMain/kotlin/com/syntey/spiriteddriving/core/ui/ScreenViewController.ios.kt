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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Hosts a screen's Compose content in native iOS navigation, which shows it in SwiftUI through `ComposeView`.
 *
 * Features call it from their `*ScreenUiController` functions; Swift calls those instead.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun screenViewController(content: @Composable () -> Unit): UIViewController = themedViewController(
    background = { MaterialTheme.colorScheme.background },
) {
    // SwiftUI lays the screen out under native bars (see `composeScreen()` in Swift), so that the background reaches
    // behind them. The bars, together with the keyboard, are then kept clear of content here.
    Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        content()
    }
}

/**
 * Like [screenViewController], for a screen in a native sheet's navigation stack, such as Profile: the sheet's colour is
 * `surfaceContainerLow`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun sheetScreenViewController(content: @Composable () -> Unit): UIViewController = themedViewController(
    background = { MaterialTheme.colorScheme.surfaceContainerLow },
) {
    Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        content()
    }
}

/**
 * Like [screenViewController], for a screen that draws behind the status bar and the home indicator, such as one with a
 * full-screen illustration. The screen keeps its own controls clear of them, through `WindowInsets.safeDrawing`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun fullScreenViewController(content: @Composable () -> Unit): UIViewController = themedViewController(
    background = { MaterialTheme.colorScheme.background },
    content = content,
)

/**
 * Hosts Compose content on a native sheet, such as the map's. The background stays clear, so the sheet's glass shows
 * through; the content keeps clear of the sheet's grabber and edges itself.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun sheetViewController(content: @Composable () -> Unit): UIViewController = composeViewController(opaque = false) {
    AppTheme {
        content()
    }
}.also { it.view.backgroundColor = UIColor.clearColor }

@OptIn(ExperimentalComposeUiApi::class)
private fun themedViewController(
    background: @Composable () -> Color,
    content: @Composable () -> Unit,
): UIViewController = composeViewController(opaque = true) {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background(),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun composeViewController(opaque: Boolean, content: @Composable () -> Unit): UIViewController = ComposeUIViewController(
    configure = {
        this.opaque = opaque

        // Keyboard is part of the safe drawing insets applied by the screen, panning the content on top of that would move it
        // twice.
        onFocusBehavior = OnFocusBehavior.DoNothing

        // Render-thread frames hand their drawable back through the main queue, which a pushed screen's first synchronous
        // draw is blocking while it waits for one, so the native push would stall until the drawable acquisition times out.
        parallelRendering = false
    },
    content = content,
)
