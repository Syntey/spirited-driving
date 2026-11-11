package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.SheetHeaderStyle
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * The top of a sheet: a title, an optional line below it, and a close button. A tappable subtitle, such as "From My
 * location" in route planning, gets a [subtitleChevron] when one is passed and stays in the subtitle colour, since red
 * is kept for main actions.
 *
 * @param close The close button; its label is what screen readers hear.
 * @param subtitleLeading Shown before the subtitle, such as a route's badge.
 */
@Composable
fun SheetHeader(
    title: String,
    close: IconAction,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onSubtitleClick: (() -> Unit)? = null,
    subtitleChevron: Painter? = null,
    subtitleLeading: (@Composable () -> Unit)? = null,
) {
    val style = AppTheme.platformStyle.sheetHeader
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = style.titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null) {
                Row(
                    Modifier.padding(top = Grid.d1),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Grid.d2),
                ) {
                    subtitleLeading?.invoke()
                    val tapModifier = if (onSubtitleClick != null) {
                        Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onSubtitleClick)
                    } else {
                        Modifier
                    }
                    Row(tapModifier, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subtitle,
                            style = style.subtitleStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (onSubtitleClick != null && subtitleChevron != null) {
                            Icon(
                                painter = subtitleChevron,
                                contentDescription = null,
                                modifier = Modifier.size(Grid.d4).mirrorIn(LocalLayoutDirection.current),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(Grid.d3))
        // The touch target is taller than the circle; lifting the circle by the difference puts its top level with the title
        val platformStyle = AppTheme.platformStyle
        CloseButton(
            action = close,
            modifier = Modifier.offset(y = -(platformStyle.minTouchTarget - platformStyle.closeButton.size) / 2),
        )
    }
}

@ComponentPreviews
@Composable
private fun SheetHeaderPreview() = Showcase {
    SheetHeaderExamples()
}

@ComponentPreviews
@Composable
private fun SheetHeaderIosPreview() = Showcase(platform = UiPlatform.Ios) {
    SheetHeaderExamples()
}

@Composable
private fun SheetHeaderExamples() {
    val close = IconAction(icon = previewIcon(), label = "Close", onClick = {})
    Column(
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow).padding(Grid.d6),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        SheetHeader(title = "Karlštejn Castle", close = close)
        SheetHeader(
            title = "Karlštejn Castle",
            subtitle = "From My location",
            onSubtitleClick = {},
            subtitleChevron = previewIcon(),
            close = close,
        )
        SheetHeader(
            title = "How was the route to Karlštejn Castle?",
            subtitle = "Today, 14:20 · 1 h 05 · 58 km",
            subtitleLeading = {
                Box(Modifier.size(Grid.d6).clip(CircleShape).background(AppTheme.customColors.routeFun1))
            },
            close = close,
        )
    }
}
