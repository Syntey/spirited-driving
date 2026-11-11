package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.LayoutDirection

/**
 * Flips a directional icon, such as a chevron or Back, in right-to-left layouts; the SVG icons don't mirror themselves.
 */
fun Modifier.mirrorIn(layoutDirection: LayoutDirection): Modifier =
    if (layoutDirection == LayoutDirection.Rtl) scale(scaleX = -1f, scaleY = 1f) else this
