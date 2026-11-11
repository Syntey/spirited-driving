package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.IconButtonStyle
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** The colours of an [AppIconButton], taken from [IconButtonDefaultColors]. */
@Immutable
data class AppIconButtonColors(val containerColor: Color, val contentColor: Color, val disabledContentColor: Color)

/**
 * The colours of [AppIconButton]. Plain buttons have no container; filled buttons get a faint one that shows on both glass
 * and solid sheets.
 */
object IconButtonDefaultColors {

    @Composable
    fun plain(): AppIconButtonColors = AppIconButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
    )

    @Composable
    fun filled(): AppIconButtonColors = AppIconButtonColors(
        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = FILLED_CONTAINER_ALPHA),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
    )

    // Material's disabled alpha, as in ButtonDefaultColors
    private const val DISABLED_CONTENT_ALPHA = 0.38f
    private const val FILLED_CONTAINER_ALPHA = 0.08f
}

/**
 * A round button with only an icon. [contentDescription] is what screen readers hear.
 *
 * The circle is [IconButtonStyle.size], but the tappable area is at least the platform's touch target, so a small circle
 * still lays out at 44 pt or 48 dp. The circle is drawn inside that area rather than sized to it, which is why this isn't
 * a material3 `IconButton`: its size modifier would clamp the touch target to the circle.
 */
@Composable
fun AppIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    style: IconButtonStyle,
    modifier: Modifier = Modifier,
    colors: AppIconButtonColors = if (style.isFilled) IconButtonDefaultColors.filled() else IconButtonDefaultColors.plain(),
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(style.size)
            .clip(CircleShape)
            .background(colors.containerColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(style.iconSize),
            tint = if (enabled) colors.contentColor else colors.disabledContentColor,
        )
    }
}

/** Closes a sheet or a step in it. The action's label is what screen readers hear. */
@Composable
fun CloseButton(action: IconAction, modifier: Modifier = Modifier) {
    AppIconButton(
        icon = action.icon,
        contentDescription = action.label,
        onClick = action.onClick,
        style = AppTheme.platformStyle.closeButton,
        modifier = modifier,
    )
}

/**
 * Opens a menu, with three dots: vertical on Android, horizontal on iOS. [contentDescription] names what it's for, such as
 * "More for Karlštejn Castle".
 */
@Composable
fun MoreButton(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    val horizontal = AppTheme.platformStyle.moreIsHorizontal
    AppIconButton(
        icon = remember(horizontal) { MoreDotsPainter(horizontal) },
        contentDescription = contentDescription,
        onClick = onClick,
        style = AppTheme.platformStyle.moreButton,
        modifier = modifier,
    )
}

// The dots are drawn rather than taken from an icon, so the platform's arrangement needs no branch in a screen. The
// colour filter is kept so the dots take the tint of the Icon around them, as an SVG icon does.
private class MoreDotsPainter(private val horizontal: Boolean) : Painter() {

    private var filter: ColorFilter? = null

    override val intrinsicSize: Size = Size.Unspecified

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        filter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        val radius = size.minDimension * DOT_RADIUS
        val spacing = size.minDimension * DOT_SPACING
        val middle = Offset(size.width / 2, size.height / 2)
        for (step in -1..1) {
            val shift = step * spacing
            val dot = if (horizontal) Offset(middle.x + shift, middle.y) else Offset(middle.x, middle.y + shift)
            drawCircle(color = Color.Black, radius = radius, center = dot, colorFilter = filter)
        }
    }
}

// The dots' radius and spacing as fractions of the icon, so they scale with it
private const val DOT_RADIUS = 0.08f
private const val DOT_SPACING = 0.27f

@ComponentPreviews
@Composable
private fun IconButtonsPreview() = Showcase {
    Row(
        Modifier.padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CloseButton(IconAction(previewIcon(), "Close", {}))
        MoreButton(onClick = {}, contentDescription = "More")
        AppIconButton(
            icon = previewIcon(),
            contentDescription = "Delete",
            onClick = {},
            style = AppTheme.platformStyle.moreButton,
            enabled = false,
        )
    }
}

@ComponentPreviews
@Composable
private fun IconButtonsIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Row(
        Modifier.padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CloseButton(IconAction(previewIcon(), "Close", {}))
        MoreButton(onClick = {}, contentDescription = "More")
        AppIconButton(
            icon = previewIcon(),
            contentDescription = "Delete",
            onClick = {},
            style = AppTheme.platformStyle.moreButton,
            enabled = false,
        )
    }
}
