package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The reminder's content: the route's badge, the question with the destination and the day under it, and a close button.
 * The tappable part is the badge and the text; the close button dismisses. It has no background or height, as the map
 * gives it its surface.
 */
@Composable
internal fun ReminderBubbleContent(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    // Both texts are null only without a reminder, which the map doesn't show the bubble for
    val number = viewState.reminder?.question?.routeNumber ?: return
    val question = stringResource(MR.strings.drive_feedback_question_unnamed)
    val details = viewState.reminderText?.localized().orEmpty()
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { contentDescription = "$question $details" }
                .clickable(onClick = actions::onReminderClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Grid.d3),
        ) {
            RatingRouteBadge(number = number)
            Column(Modifier.weight(1f)) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = actions::onReminderDismiss) {
            Icon(
                painter = painterResource(MR.images.close),
                contentDescription = stringResource(MR.strings.generic_close),
                modifier = Modifier.size(Grid.d6),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun ReminderBubbleNamedPreview() = Showcase {
    ReminderBubblePreviewFrame(viewState = TripPlanPreviewStates.reminderViewState())
}

@ComponentPreviews
@Composable
private fun ReminderBubbleUnnamedPreview() = Showcase {
    ReminderBubblePreviewFrame(viewState = TripPlanPreviewStates.reminderViewState(destinationName = null))
}

// The map gives the bubble its surface and height; the previews stand in for it
@Composable
private fun ReminderBubblePreviewFrame(viewState: TripPlanViewState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(percent = 50),
        modifier = Modifier.fillMaxWidth().height(Grid.d14),
    ) {
        ReminderBubbleContent(
            viewState = viewState,
            actions = noOpActions(),
            modifier = Modifier.padding(horizontal = Grid.d4),
        )
    }
}
