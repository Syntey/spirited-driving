package com.syntey.spiriteddriving.core.ui.glass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState

/**
 * A surface of glass over content that floats behind it, such as an illustration or a scrolling list. The content behind
 * is marked with `Modifier.hazeSource(state)`.
 *
 * Each platform has its own glass: Liquid Glass style on iOS (Haze Glass), frosted glass on Android (Haze blur). Both are
 * tinted with the theme's surface colour and follow the light or dark theme. Not for use over the map, which is a native
 * view that Haze can't see.
 */
@Composable
expect fun GlassSurface(
    state: HazeState,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
)
