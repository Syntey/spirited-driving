package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

/**
 * An action shown with an icon, such as Close, Try again or Delete. [label] is the button's text, or what screen readers
 * hear when only the icon shows.
 */
@Immutable
data class IconAction(val icon: Painter, val label: String, val onClick: () -> Unit)
