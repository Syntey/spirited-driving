package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.withTabularFigures

/**
 * A route's label on the map, a pill in its route colour: "1 · +27 min" for a fun route, "Fastest · 38 min" for the fastest.
 * It sits on the route's line, so the line it labels is clear. Colour is never the only signal: the text always names the
 * route.
 *
 * @param text The label, already localized: the number or "Fastest" always shows.
 * @param color The route's colour, the pill's fill.
 * @param outline The casing's colour, the pill's border, so the label reads over any road colour.
 */
@Composable
fun RouteLabel(text: String, color: Color, outline: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color)
            .border(Grid.d0_375, outline, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Grid.d2, vertical = Grid.d0_5),
        style = MaterialTheme.typography.labelMedium.withTabularFigures(),
        color = onRouteColor(color),
    )
}

@ComponentPreviews
@Composable
private fun RouteLabelPreview() = Showcase {
    RouteLabelExamples()
}

@Composable
private fun RouteLabelExamples() {
    val routeColors = AppTheme.customColors
    Column(
        Modifier.padding(Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d2),
    ) {
        RouteLabel(
            text = "1 · +27 min",
            color = routeColors.routeFun1,
            outline = routeColors.routeCasing,
        )
        RouteLabel(
            text = "Fastest · 38 min",
            color = routeColors.routeFastest,
            outline = routeColors.routeCasing,
        )
    }
}
