package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.layout.height
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.Content
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultColors
import com.syntey.spiriteddriving.core.ui.components.CarSpinner
import com.syntey.spiriteddriving.core.ui.components.FunMeter
import com.syntey.spiriteddriving.core.ui.components.GroupPosition
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroupDefaultColors
import com.syntey.spiriteddriving.core.ui.components.PlaceholderRow
import com.syntey.spiriteddriving.core.ui.components.RouteBadge
import com.syntey.spiriteddriving.core.ui.components.SheetHeader
import com.syntey.spiriteddriving.core.ui.components.SheetHeaderTextButton
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.withTabularFigures
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList

/** The top of route planning in the sheet: where the trip goes, where it starts, and a Cancel text button. */
@Composable
internal fun RoutePlanningHeader(destination: Place, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    SheetHeader(
        title = destination.name ?: stringResource(MR.strings.search_point_on_map),
        subtitle = stringResource(MR.strings.planning_from_my_location),
        // Cancel, not an X: the sheet stays, and planning steps back to the search
        trailing = {
            SheetHeaderTextButton(text = stringResource(MR.strings.generic_cancel), onClick = actions::onRoutePlanningClose)
        },
        modifier = modifier,
    )
}

/**
 * The part of route planning below the header: the routes, or what takes their place while they're planned, then Save and
 * Drive, which stay in every state.
 */
@Composable
internal fun RoutePlanningBody(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    val planning = viewState.planning ?: return
    val style = MapPlatformStyle.current
    val direction = LocalLayoutDirection.current
    val padding = style.sheetContentPadding
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = padding.calculateStartPadding(direction),
                end = padding.calculateEndPadding(direction),
                bottom = padding.calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (planning.status) {
                PlanningStatus.Planning -> PlanningList()

                PlanningStatus.Planned -> RouteList(rows = viewState.routeRows, onRouteClick = actions::onRouteClick)

                PlanningStatus.Offline -> StateMessage(
                    icon = painterResource(MR.images.offline),
                    title = stringResource(MR.strings.planning_offline_title),
                    message = stringResource(MR.strings.planning_offline_text),
                    retry = retryAction(actions),
                    modifier = Modifier.fillMaxSize(),
                )

                PlanningStatus.Failed -> StateMessage(
                    icon = painterResource(MR.images.warning),
                    title = stringResource(MR.strings.planning_failed_title),
                    message = stringResource(MR.strings.planning_failed_text),
                    retry = retryAction(actions),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
            // Save waits for Account, the sign-in it needs; Drive waits for the Drive feature
            AppButton(
                text = stringResource(MR.strings.planning_save),
                onClick = {},
                colors = ButtonDefaultColors.tonal(),
                enabled = false,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = stringResource(MR.strings.planning_drive),
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(2f),
            )
        }
        Text(
            text = stringResource(MR.strings.planning_attribution),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun retryAction(actions: TripPlanScreen.Actions): IconAction = IconAction(
    icon = painterResource(MR.images.refresh),
    label = stringResource(MR.strings.generic_try_again),
    onClick = actions::onPlanningRetry,
)

// Placeholder rows where the routes will be, with a spinner and what's happening above them
@Composable
private fun PlanningList() {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
                CarSpinner(Modifier.size(Grid.d4_5), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = stringResource(MR.strings.planning_looking),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(PLACEHOLDER_ROWS) { index ->
            PlaceholderRow(
                position = GroupPosition.of(index, PLACEHOLDER_ROWS),
                groupColors = ListGroupDefaultColors.translucent(),
            )
        }
    }
}

// On iOS the rows sit on one translucent card, inset from its edge so the rows' corners stay inside it. The card ends
// below the last row; the list scrolls inside it when the rows don't fit.
@Composable
private fun RouteList(rows: ImmutableList<RouteRow>, onRouteClick: (String) -> Unit) {
    val style = MapPlatformStyle.current.routeList
    val card = if (style.rowsOnCard) {
        Modifier.clip(AppTheme.platformStyle.cardShape).background(ListGroupDefaultColors.translucent().containerColor)
    } else {
        Modifier.fillMaxHeight()
    }
    LazyColumn(
        Modifier.fillMaxWidth().then(card),
        contentPadding = PaddingValues(if (style.rowsOnCard) Grid.d1 else Grid.d0),
        verticalArrangement = Arrangement.spacedBy(Grid.d1),
    ) {
        items(rows, key = { it.id }) { row ->
            RouteRowItem(row = row, onClick = { onRouteClick(row.id) })
        }
    }
}

// One route: its badge, the extra over the fastest route, the time and distance, the facts about its roads, and the fun meter.
// The row is one item for screen readers, with its selection.
@Composable
private fun RouteRowItem(row: RouteRow, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = routeColor(row.number)
    val style = MapPlatformStyle.current.routeList
    val shape = style.rowShape
    val selectedFill = if (style.isSelectedRowLifted) {
        AppTheme.customColors.listGroup
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val badgeDescription = row.number?.let { stringResource(MR.strings.route_badge, it) }
        ?: stringResource(MR.strings.route_badge_fastest)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { selected = row.isSelected }
            .clip(shape)
            .background(if (row.isSelected) selectedFill else Color.Transparent)
            .then(if (row.isSelected) Modifier.border(Grid.d0_5, color, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d3),
        verticalAlignment = Alignment.Top,
    ) {
        RouteBadge(
            color = color,
            contentDescription = badgeDescription,
            number = row.number,
            fastestIcon = if (row.number == null) painterResource(MR.images.fastest) else null,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Grid.d0_5)) {
            Text(
                text = row.extra?.localized() ?: stringResource(MR.strings.planning_fastest),
                style = MaterialTheme.typography.titleMedium.withTabularFigures(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${row.duration.localized()} · ${row.distance.localized()}",
                style = MaterialTheme.typography.bodyMedium.withTabularFigures(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(MR.strings.planning_route_facts, row.curvyDistance.localized(), row.ascent.localized()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FunMeter(
            level = row.funLevel,
            label = stringResource(MR.strings.fun_meter),
            contentDescription = stringResource(MR.strings.fun_meter_description, row.funLevel),
        )
    }
}

private const val PLACEHOLDER_ROWS = 3

@ComponentPreviews
@Composable
private fun RoutePlanningPlanningPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.planningViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlanningIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.planningViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.plannedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.plannedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNoFunRoutePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.noFunRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNoFunRouteIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.noFunRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningOfflinePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.planningOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.planningOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningFailedPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.planningFailedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.planningFailedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
