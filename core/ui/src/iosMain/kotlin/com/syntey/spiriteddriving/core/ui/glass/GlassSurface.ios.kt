package com.syntey.spiriteddriving.core.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.glass.material3.material3

@Composable
actual fun GlassSurface(
    state: HazeState,
    shape: RoundedCornerShape,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = remember(shape) { GlassStyle.regular.then { shape(shape) } }.material3()
    Box(
        modifier = modifier.hazeGlass(input = HazeInput.Backdrop(state), style = style),
        content = content,
    )
}
