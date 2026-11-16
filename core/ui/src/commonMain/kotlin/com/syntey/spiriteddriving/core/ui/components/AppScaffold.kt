package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * The frame of a screen: its bars around the content, on both platforms.
 *
 * @param topBar Usually an [AndroidTopBar]; on iOS the native navigation bar takes its place.
 * @param content The screen, given the padding that keeps it clear of the bars.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        // On iOS the native navigation bar and sheet already keep the content inside the safe area
        contentWindowInsets = if (AppTheme.platform == UiPlatform.Ios) {
            WindowInsets(0)
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        content = content,
    )
}

@ComponentPreviews
@Composable
private fun AppScaffoldPreview() = Showcase {
    AppScaffold(
        topBar = {
            AndroidTopBar(
                title = "Settings",
                navigation = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@ComponentPreviews
@Composable
private fun AppScaffoldIosPreview() = Showcase(platform = UiPlatform.Ios) {
    AppScaffold(
        topBar = {
            AndroidTopBar(
                title = "Settings",
                navigation = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}
