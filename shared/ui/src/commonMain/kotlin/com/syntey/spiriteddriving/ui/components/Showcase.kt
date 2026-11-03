package com.syntey.spiriteddriving.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.ui.theme.MyApplicationTheme

/**
 * A @Composable wrapper that displays content on a Surface with application theme.
 */
@Composable
fun Showcase(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    MyApplicationTheme {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
