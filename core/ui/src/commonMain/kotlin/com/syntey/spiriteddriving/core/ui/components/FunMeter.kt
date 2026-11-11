package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * How much fun a route is: five rising bars in amber, as the theme gives ratings to tertiary, with [label] below them.
 * One scale for every route, the fastest included. Screen readers hear [contentDescription] instead.
 */
@Composable
fun FunMeter(
    level: Int,
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    require(level in 0..MAX_LEVEL) { "A fun level is 0 to $MAX_LEVEL, not $level" }
    val barColor = MaterialTheme.colorScheme.tertiary
    val emptyBarColor = barColor.copy(alpha = EMPTY_BAR_ALPHA)

    Column(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(width = Grid.d7, height = Grid.d5)) {
            val barWidth = Grid.d1.toPx()
            val gap = Grid.d0_5.toPx()
            val corner = CornerRadius(Grid.d0_5.toPx())
            for (bar in 1..MAX_LEVEL) {
                val barHeight = size.height * bar / MAX_LEVEL
                drawRoundRect(
                    color = if (bar <= level) barColor else emptyBarColor,
                    topLeft = Offset(x = (bar - 1) * (barWidth + gap), y = size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner,
                )
            }
        }
        Spacer(Modifier.height(Grid.d0_5))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// The number of bars is the highest level, so every bar stands for one level
private const val MAX_LEVEL = 5

// The empty bars still show the scale, so a low rating reads as low rather than as missing
private const val EMPTY_BAR_ALPHA = 0.25f

@ComponentPreviews
@Composable
private fun FunMeterPreview() = Showcase {
    FunMeterExamples()
}

@ComponentPreviews
@Composable
private fun FunMeterIosPreview() = Showcase(platform = UiPlatform.Ios) {
    FunMeterExamples()
}

@Composable
private fun FunMeterExamples() {
    Row(
        Modifier.padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d4),
    ) {
        FunMeter(level = 1, label = "Fun", contentDescription = "Fun 1 of 5")
        FunMeter(level = 3, label = "Fun", contentDescription = "Fun 3 of 5")
        FunMeter(level = 5, label = "Fun", contentDescription = "Fun 5 of 5")
    }
}
