package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * Takes the place of a list or a result with nothing to show, whether it's empty or something failed: an icon, what
 * happened, and what it means or what to do. It stays neutral, since being offline or a failure on our side isn't the
 * user's mistake. Given more room than it needs, it sits a little above the middle.
 *
 * @param retry Shows the action's icon and label, such as Try again; pass it only when trying again can help.
 */
@Composable
fun StateMessage(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retry: IconAction? = null,
) {
    Box(modifier) {
        Column(
            Modifier.align(BiasAlignment(0f, ABOVE_MIDDLE)).padding(horizontal = Grid.d6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(Grid.d16).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Grid.d8),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.height(Grid.d3))
            Column(Modifier.semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Grid.d1))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (retry != null) {
                Spacer(Modifier.height(Grid.d4))
                AppButton(
                    text = retry.label,
                    onClick = retry.onClick,
                    colors = ButtonDefaultColors.tonal(),
                    style = ButtonDefaultStyles.medium(),
                    leadingIcon = retry.icon,
                )
            }
        }
    }
}

// A little above the middle, where the eye expects it
private const val ABOVE_MIDDLE = -0.2f

@ComponentPreviews
@Composable
private fun StateMessagePreview() = Showcase {
    StateMessageExamples()
}

@ComponentPreviews
@Composable
private fun StateMessageIosPreview() = Showcase(platform = UiPlatform.Ios) {
    StateMessageExamples()
}

@Composable
private fun StateMessageExamples() {
    Column(
        Modifier.fillMaxWidth().padding(Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        StateMessage(
            icon = previewIcon(),
            title = "You're offline",
            message = "Search needs a connection.",
            retry = IconAction(icon = previewIcon(), label = "Try again", onClick = {}),
            modifier = Modifier.fillMaxWidth(),
        )
        StateMessage(
            icon = previewIcon(),
            title = "No places match \"karlsxyz\"",
            message = "Check the spelling, or touch and hold the map to pick a point.",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
