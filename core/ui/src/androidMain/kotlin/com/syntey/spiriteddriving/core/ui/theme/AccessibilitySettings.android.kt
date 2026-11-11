package com.syntey.spiriteddriving.core.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun ProvideAccessibilitySettings(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // "Remove animations" sets the animator duration scale to 0.
    val reduceMotion = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    CompositionLocalProvider(LocalReduceMotion provides reduceMotion, content = content)
}
