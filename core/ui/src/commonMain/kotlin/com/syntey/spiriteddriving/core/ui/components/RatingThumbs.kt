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

/** A rating of a route: thumbs up or down. */
enum class Thumb { Up, Down }

/** One of the two buttons of [RatingThumbs]: its label, and its icon unpicked and picked. */
@Immutable
data class ThumbContent(val label: String, val icon: Painter, val pickedIcon: Painter)

/**
 * Good and Not good, two equal buttons side by side. The picked one is amber with its picked icon. Screen readers hear
 * them as one group labelled [groupLabel], with the picked one selected.
 */
@Composable
fun RatingThumbs(
    picked: Thumb?,
    onPick: (Thumb) -> Unit,
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
        ThumbButton(thumb = Thumb.Up, thumbContent = good, picked = picked, onPick = onPick)
        ThumbButton(thumb = Thumb.Down, thumbContent = notGood, picked = picked, onPick = onPick)
    }
}

@Composable
private fun RowScope.ThumbButton(thumb: Thumb, thumbContent: ThumbContent, picked: Thumb?, onPick: (Thumb) -> Unit) {
    val isPicked = picked == thumb
    AppButton(
        text = thumbContent.label,
        onClick = { onPick(thumb) },
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
    val good = ThumbContent(label = "Good", icon = previewIcon(), pickedIcon = previewIcon())
    val notGood = ThumbContent(label = "Not good", icon = previewIcon(), pickedIcon = previewIcon())
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d3)) {
        RatingThumbs(picked = null, onPick = {}, groupLabel = "Rate the route", good = good, notGood = notGood)
        RatingThumbs(picked = Thumb.Up, onPick = {}, groupLabel = "Rate the route", good = good, notGood = notGood)
    }
}
