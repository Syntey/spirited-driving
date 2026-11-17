package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** One of the two buttons of [RatingThumbs]: its label, its icon unpicked and picked, whether it's picked, and its tap. */
@Immutable
data class ThumbContent(val label: String, val icon: Painter, val pickedIcon: Painter, val picked: Boolean, val onClick: () -> Unit)

/**
 * Good and Not good, two equal buttons side by side. The picked one is amber with its picked icon. Screen readers hear
 * them as one group labelled [groupLabel], with the picked one selected.
 */
@Composable
fun RatingThumbs(
    groupLabel: String,
    good: ThumbContent,
    notGood: ThumbContent,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .selectableGroup()
            .semantics { contentDescription = groupLabel },
        horizontalArrangement = Arrangement.spacedBy(Grid.d3),
    ) {
        ThumbButton(good)
        ThumbButton(notGood)
    }
}

@Composable
private fun RowScope.ThumbButton(thumbContent: ThumbContent) {
    val isPicked = thumbContent.picked
    AppButton(
        text = thumbContent.label,
        onClick = thumbContent.onClick,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .semantics { selected = isPicked },
        colors = ButtonDefaultColors.toggle(isPicked),
        style = ButtonDefaultStyles.toggle(),
        leadingIcon = if (isPicked) thumbContent.pickedIcon else thumbContent.icon,
    )
}

@ComponentPreviews
@Composable
private fun RatingThumbsPreview() = Showcase { RatingThumbsExamples() }

@ComponentPreviews
@Composable
private fun RatingThumbsIosPreview() = Showcase(platform = UiPlatform.Ios) { RatingThumbsExamples() }

@Composable
private fun RatingThumbsExamples() {
    val good = ThumbContent(label = "Good", icon = previewIcon(), pickedIcon = previewIcon(), picked = false, onClick = {})
    val notGood = ThumbContent(label = "Not good", icon = previewIcon(), pickedIcon = previewIcon(), picked = false, onClick = {})
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        RatingThumbs(groupLabel = "Rate the route", good = good, notGood = notGood)
        RatingThumbs(groupLabel = "Rate the route", good = good.copy(picked = true), notGood = notGood)
    }
}
