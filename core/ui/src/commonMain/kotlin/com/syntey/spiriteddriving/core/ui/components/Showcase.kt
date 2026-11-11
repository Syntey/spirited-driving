package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * A @Composable wrapper that displays content on a Surface with application theme.
 *
 * @param platform Whose look to preview. Android Studio renders both, so a preview can show the iOS look as well.
 */
@Composable
fun Showcase(
    modifier: Modifier = Modifier,
    platform: UiPlatform = UiPlatform.Current,
    content: @Composable () -> Unit,
) {
    AppTheme(platform = platform) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
