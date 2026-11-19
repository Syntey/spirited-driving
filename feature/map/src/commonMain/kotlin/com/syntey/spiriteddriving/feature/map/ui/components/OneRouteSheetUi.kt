package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.SheetHeader
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.Content
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource

/** The top of a saved route's step: its name, the start's area under it and a close button. The start can't be changed. */
@Composable
internal fun OneRouteHeader(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    val title = viewState.oneRouteTitle ?: return
    SheetHeader(
        title = title,
        subtitle = viewState.oneRouteSubtitle?.localized(),
        close = IconAction(
            icon = painterResource(MR.images.close),
            label = stringResource(MR.strings.generic_close),
            onClick = actions::onOneRouteClose,
        ),
        modifier = modifier,
    )
}

/**
 * Everything below the header: the summary, the note about the start, then Save and Drive with the attribution. It scrolls
 * when the sheet is shorter than its content.
 */
@Composable
internal fun OneRouteBody(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        OneRouteSummary(viewState = viewState, actions = actions, isCollapsed = false)
        OneRouteStartNote(viewState = viewState)
        RoutePlanningActions(viewState = viewState, actions = actions)
    }
}

/** The header, the summary, then Save and Drive: the collapsed sheet's layout of a saved route. */
@Composable
internal fun CollapsedOneRoute(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().padding(style.sheetContentPadding),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        OneRouteHeader(viewState = viewState, actions = actions)
        OneRouteSummary(viewState = viewState, actions = actions, isCollapsed = true)
        RoutePlanningActions(viewState = viewState, actions = actions)
    }
}

// The route's facts once loaded, the planning progress while it loads, and why it can't load, with Try again where it helps
@Composable
private fun OneRouteSummary(
    viewState: TripPlanViewState,
    actions: TripPlanScreen.Actions,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier,
) {
    val step = viewState.oneRouteStep ?: return
    Column(modifier.fillMaxWidth()) {
        when (step.status) {
            OneRouteStatus.Loading -> PlanningProgress()

            OneRouteStatus.Ready -> viewState.oneRouteRow?.let { row ->
                RouteRowContent(row = row, showsFacts = !isCollapsed, showsExtra = false)
            }

            OneRouteStatus.Offline -> OneRouteFailure(
                icon = MR.images.offline,
                title = MR.strings.planning_offline_title,
                message = MR.strings.one_route_offline_text,
                isCollapsed = isCollapsed,
                actions = actions,
            )

            OneRouteStatus.Failed -> OneRouteFailure(
                icon = MR.images.warning,
                title = MR.strings.planning_failed_title,
                message = MR.strings.planning_failed_text,
                isCollapsed = isCollapsed,
                actions = actions,
            )
        }
    }
}

// Collapsed, only the title, as the collapsed route planning says what's wrong; full, the message with Try again
@Composable
private fun OneRouteFailure(
    icon: ImageResource,
    title: StringResource,
    message: StringResource,
    isCollapsed: Boolean,
    actions: TripPlanScreen.Actions,
) {
    if (isCollapsed) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(Grid.d5),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        StateMessage(
            icon = painterResource(icon),
            title = stringResource(title),
            message = stringResource(message),
            retry = IconAction(
                icon = painterResource(MR.images.refresh),
                label = stringResource(MR.strings.generic_try_again),
                onClick = actions::onOneRouteRetry,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// Without location access the note says where the route starts; with it, the start's distance comes with the actions
@Composable
private fun OneRouteStartNote(viewState: TripPlanViewState) {
    val start = viewState.oneRouteStep?.origin?.start?.shortArea ?: return
    if (viewState.locationAccess.allowsLocation) return
    Text(
        text = stringResource(MR.strings.saved_route_starts_in, start),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteReadyPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.oneRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteReadyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.oneRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRoutePastDrivePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.pastDriveViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRoutePastDriveIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.pastDriveViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteLoadingPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.oneRouteViewState(status = OneRouteStatus.Loading),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteOfflinePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.oneRouteViewState(status = OneRouteStatus.Offline),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteCollapsedPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.oneRouteViewState(sheetHeight = SheetHeight.Collapsed),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
