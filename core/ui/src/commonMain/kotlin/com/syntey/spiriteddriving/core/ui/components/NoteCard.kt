package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * The colours of a [NoteCard], taken from [NoteDefaultColors]. [announces] is set for an error, so screen readers say it
 * when it appears.
 */
@Immutable
data class NoteColors(val containerColor: Color, val contentColor: Color, val actionColors: AppButtonColors, val announces: Boolean)

/**
 * The colours of [NoteCard]: a hint on tertiary, a failed action on error.
 */
object NoteDefaultColors {

    /** A hint, such as "Precise location is off", on tertiaryContainer, as the theme gives hints to tertiary. */
    @Composable
    fun hint(): NoteColors = NoteColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        actionColors = ButtonDefaultColors.tertiary(),
        announces = false,
    )

    /** A failed action the user can try again right there, such as signing in, on errorContainer. */
    @Composable
    fun error(): NoteColors = NoteColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        actionColors = ButtonDefaultColors.error(),
        announces = true,
    )
}

/**
 * A note inside a sheet or screen: optional icon and title, the text, an optional action and a close button.
 * Error notes are announced by screen readers when they appear.
 */
@Composable
fun NoteCard(
    text: String,
    modifier: Modifier = Modifier,
    colors: NoteColors = NoteDefaultColors.hint(),
    title: String? = null,
    icon: Painter? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    close: IconAction? = null,
) {
    val announceModifier = if (colors.announces) {
        Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    } else {
        Modifier
    }
    Surface(
        modifier = modifier.fillMaxWidth().then(announceModifier),
        shape = AppTheme.platformStyle.cardShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
    ) {
        Row(Modifier.padding(Grid.d4), verticalAlignment = Alignment.Top) {
            if (icon != null) {
                Icon(painter = icon, contentDescription = null, modifier = Modifier.size(Grid.d6))
                Spacer(Modifier.width(Grid.d3))
            }
            Column(Modifier.weight(1f)) {
                if (title != null) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(Grid.d0_5))
                }
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(Grid.d3))
                    AppButton(
                        text = actionLabel,
                        onClick = onAction,
                        colors = colors.actionColors,
                        style = ButtonDefaultStyles.small(),
                    )
                }
            }
            if (close != null) {
                Spacer(Modifier.width(Grid.d2))
                // Same lift as SheetHeader: the touch target is taller than the circle
                val platformStyle = AppTheme.platformStyle
                CloseButton(
                    action = close,
                    modifier = Modifier.offset(y = -(platformStyle.minTouchTarget - platformStyle.closeButton.size) / 2),
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun NoteCardPreview() = Showcase {
    NoteCardExamples()
}

@ComponentPreviews
@Composable
private fun NoteCardIosPreview() = Showcase(platform = UiPlatform.Ios) {
    NoteCardExamples()
}

@Composable
private fun NoteCardExamples() {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        NoteCard(
            title = "Precise location is off",
            text = "Turn it on to start routes where you are.",
            actionLabel = "Turn on",
            onAction = {},
            close = IconAction(icon = previewIcon(), label = "Close", onClick = {}),
        )
        NoteCard(
            text = "You're outside the covered area. Routes start and end in Czechia for now. Search for a destination " +
                "there and pick the start by hand.",
        )
        NoteCard(
            text = "Couldn't sign in. Check your connection and try again.",
            icon = previewIcon(),
            colors = NoteDefaultColors.error(),
        )
    }
}
