package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.theme.withTabularFigures

/**
 * A route's badge, in its route colour: its number, or an arrow for the fastest route. The same on the map and in the
 * list, so the user can match them. Colour is never the only signal: the number or arrow always shows.
 *
 * @param contentDescription What screen readers hear, such as "Route 2" or "Fastest route".
 * @param number The fun route's number; null for the fastest route.
 * @param fastestIcon The arrow of the fastest route, shown when [number] is null.
 */
@Composable
fun RouteBadge(
    color: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    number: Int? = null,
    fastestIcon: Painter? = null,
) {
    require(number != null || fastestIcon != null) { "A route badge needs a number, or the fastest icon" }
    val content = onRouteColor(color)
    val textStyle = MaterialTheme.typography.labelLarge.withTabularFigures()
    // The badge and icon grow by the same factor as the number, so the number keeps its room at any font size. Android
    // scales large text less than small text, so the factor comes from the number's own size, not from the badge's
    val fontScale = with(LocalDensity.current) { textStyle.fontSize.toPx() / (textStyle.fontSize.value * density) }
    val badgeSize = Grid.d6 * fontScale
    val iconSize = Grid.d4 * fontScale

    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (number != null) {
            Text(
                text = number.toString(),
                style = textStyle,
                color = content,
            )
        } else if (fastestIcon != null) {
            Icon(
                painter = fastestIcon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = content,
            )
        }
    }
}

/**
 * The text colour on a route colour: dark on the lighter route colours of the dark theme, white on the darker ones of the
 * light theme, so the text always contrasts with its colour.
 */
internal fun onRouteColor(color: Color): Color =
    if (color.luminance() > LIGHT_LUMINANCE) OnLightRoute else Color.White

// Light route colours in the dark theme and dark ones in the light theme: the text has to contrast with the colour
private const val LIGHT_LUMINANCE = 0.4f

// The light theme's onSurface, the text on the lighter route colours of the dark theme
private val OnLightRoute = Color(0xFF1A1C1E)

@ComponentPreviews
@Composable
private fun RouteBadgePreview() = Showcase {
    RouteBadgeExamples()
}

@ComponentPreviews
@Composable
private fun RouteBadgeIosPreview() = Showcase(platform = UiPlatform.Ios) {
    RouteBadgeExamples()
}

@Composable
private fun RouteBadgeExamples() {
    val routeColors = AppTheme.customColors
    Row(
        Modifier.padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d3),
    ) {
        RouteBadge(routeColors.routeFastest, contentDescription = "Fastest route", fastestIcon = previewIcon())
        RouteBadge(routeColors.routeFun1, contentDescription = "Route 1", number = 1)
        RouteBadge(routeColors.routeFun2, contentDescription = "Route 2", number = 2)
        RouteBadge(routeColors.routeFun3, contentDescription = "Route 3", number = 3)
    }
}
