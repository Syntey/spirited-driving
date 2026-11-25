package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning

import androidx.compose.foundation.layout.height
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.Content
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningPreviewStates
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
import androidx.compose.material3.Icon
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
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultStyles
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
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import com.syntey.spiriteddriving.feature.map.model.ui.NoFunRouteMessage
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import com.syntey.spiriteddriving.feature.map.model.ui.distanceText
import com.syntey.spiriteddriving.feature.map.ui.components.BudgetPicker
import com.syntey.spiriteddriving.feature.map.ui.components.MapPlatformStyle
import com.syntey.spiriteddriving.feature.map.ui.components.routeColor
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList

/**
 * The top of route planning in the sheet: where the trip goes, where it starts, and a Cancel text button. The start is
 * tappable: it opens the start step, where the user changes it.
 */
@Composable
internal fun RoutePlanningHeader(
    planning: RoutePlanningState,
    planningActions: RoutePlanningScreen.Actions,
    modifier: Modifier = Modifier,
) {
    val start = planning.start
    SheetHeader(
        title = planning.destination.name ?: stringResource(MR.strings.search_point_on_map),
        subtitle = if (start == null) {
            stringResource(MR.strings.planning_from_my_location)
        } else {
            stringResource(MR.strings.planning_from, start.name ?: stringResource(MR.strings.search_point_on_map))
        },
        onSubtitleClick = planningActions::onStartClick,
        subtitleChevron = painterResource(MR.images.chevron_right),
        // Cancel, not an X: the sheet stays, and planning steps back to the search
        trailing = {
            SheetHeaderTextButton(text = stringResource(MR.strings.generic_cancel), onClick = planningActions::onCloseClick)
        },
        modifier = modifier,
    )
}

/**
 * The part of route planning below the header: the budget, the routes or what takes their place while they're planned, then
 * Save and Drive. The budget and Save and Drive stay in every state.
 */
@Composable
internal fun RoutePlanningBody(
    planning: RoutePlanningViewState,
    planningActions: RoutePlanningScreen.Actions,
    viewState: MapSheetViewState,
    actions: MapSheetScreen.Actions,
    modifier: Modifier = Modifier,
) {
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
        BudgetPicker(
            kind = planning.budgetKind,
            unit = planning.distanceUnit,
            chips = planning.budgetChips,
            onKindChange = planningActions::onBudgetKindChange,
            onBudgetClick = planningActions::onBudgetClick,
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (planning.planning.status) {
                PlanningStatus.Planning -> PlanningList()

                PlanningStatus.Planned -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
                ) {
                    planning.noFunRouteMessage?.let { message ->
                        NoFunRouteNote(message = message, onUseBudget = planningActions::onBudgetClick)
                    }
                    // Takes the height the note leaves, so the list stays below it
                    RouteList(
                        rows = planning.routeRows,
                        onRouteClick = planningActions::onRouteClick,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }

                PlanningStatus.NotCovered -> StateMessage(
                    icon = painterResource(MR.images.map),
                    title = stringResource(MR.strings.planning_outside_title),
                    message = stringResource(MR.strings.planning_outside_text),
                    modifier = Modifier.fillMaxSize(),
                )

                PlanningStatus.Offline -> StateMessage(
                    icon = painterResource(MR.images.offline),
                    title = stringResource(MR.strings.planning_offline_title),
                    message = stringResource(MR.strings.planning_offline_text),
                    retry = retryAction(planningActions),
                    modifier = Modifier.fillMaxSize(),
                )

                PlanningStatus.Failed -> StateMessage(
                    icon = painterResource(MR.images.warning),
                    title = stringResource(MR.strings.planning_failed_title),
                    message = stringResource(MR.strings.planning_failed_text),
                    retry = retryAction(planningActions),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        RoutePlanningActions(viewState = viewState, actions = actions)
    }
}

/**
 * Save and Drive, with the attribution under them. When the route starts far from the user, the line above them says how far
 * and that the maps app takes the user there first.
 */
@Composable
internal fun RoutePlanningActions(viewState: MapSheetViewState, actions: MapSheetScreen.Actions, modifier: Modifier = Modifier) {
    val style = MapPlatformStyle.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(style.sheetSpacing)) {
        viewState.startDistance?.let { distance ->
            Text(
                text = StringDesc.ResourceFormatted(
                    if (AppTheme.platform == UiPlatform.Android) {
                        MR.strings.drive_starts_elsewhere_google
                    } else {
                        MR.strings.drive_starts_elsewhere_apple
                    },
                    distanceText(
                        distance = distance,
                        unit = viewState.distanceUnit,
                        isApproximate = viewState.locationAccess == LocationAccess.Approximate,
                    ),
                ).localized(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
            val isSaved = viewState.isSelectedRouteSaved
            AppButton(
                text = stringResource(if (isSaved) MR.strings.planning_saved else MR.strings.planning_save),
                onClick = actions::onSaveClick,
                colors = ButtonDefaultColors.tonal(),
                leadingIcon = painterResource(if (isSaved) MR.images.bookmark_filled else MR.images.bookmark),
                isLoading = viewState.isSavingRoute,
                enabled = viewState.isDriveEnabled,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = stringResource(MR.strings.planning_drive),
                onClick = actions::onDriveClick,
                enabled = viewState.isDriveEnabled,
                modifier = Modifier.weight(2f),
            )
        }
        RouteAttribution()
    }
}

/** The data credit under a route's actions: the routing provider on Android, then the route data's attribution. */
@Composable
internal fun RouteAttribution(modifier: Modifier = Modifier) {
    val style = MapPlatformStyle.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(style.sheetSpacing)) {
        if (style.showsRoutingProvider) {
            Text(
                text = stringResource(MR.strings.drive_routing_provider),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
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

/** Route planning in the collapsed sheet: the header, the plan's summary, Save and Drive. */
@Composable
internal fun CollapsedRoutePlanning(
    planning: RoutePlanningViewState,
    planningActions: RoutePlanningScreen.Actions,
    viewState: MapSheetViewState,
    actions: MapSheetScreen.Actions,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().padding(style.sheetContentPadding),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        RoutePlanningHeader(planning = planning.planning, planningActions = planningActions)
        RoutePlanningSummary(planning = planning)
        RoutePlanningActions(viewState = viewState, actions = actions)
    }
}

/** What the collapsed sheet shows of the plan: the selected route, or what's happening instead. */
@Composable
internal fun RoutePlanningSummary(planning: RoutePlanningViewState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        when (planning.planning.status) {
            PlanningStatus.Planning -> PlanningProgress()

            PlanningStatus.Planned -> planning.selectedRouteRow?.let { row ->
                RouteRowContent(row = row, showsFacts = false, modifier = Modifier.semantics(mergeDescendants = true) {})
            }

            PlanningStatus.Offline, PlanningStatus.Failed, PlanningStatus.NotCovered -> {
                // Only the title: pulling the sheet up shows the message, and Try again where there is one
                val (icon, title) = when (planning.planning.status) {
                    PlanningStatus.Offline -> MR.images.offline to MR.strings.planning_offline_title

                    PlanningStatus.Failed -> MR.images.warning to MR.strings.planning_failed_title

                    // NotCovered; Planning and Planned are handled above
                    else -> MR.images.map to MR.strings.planning_outside_title
                }
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
            }
        }
    }
}

@Composable
internal fun retryAction(planningActions: RoutePlanningScreen.Actions): IconAction = IconAction(
    icon = painterResource(MR.images.refresh),
    label = stringResource(MR.strings.generic_try_again),
    onClick = planningActions::onRetryClick,
)

@Composable
internal fun PlanningProgress() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
        CarSpinner(Modifier.size(Grid.d4_5), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = stringResource(MR.strings.planning_looking),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Placeholder rows where the routes will be, with a spinner and what's happening above them
@Composable
private fun PlanningList() {
    LazyColumn(Modifier.fillMaxSize()) {
        item { PlanningProgress() }
        items(PLACEHOLDER_ROWS) { index ->
            PlaceholderRow(
                position = GroupPosition.of(index, PLACEHOLDER_ROWS),
                groupColors = ListGroupDefaultColors.translucent(),
                style = AppTheme.platformStyle.sheetList,
            )
        }
    }
}

// On iOS the rows sit on one translucent card, inset from its edge so the rows' corners stay inside it. The card ends
// below the last row; the list scrolls inside it when the rows don't fit.
@Composable
private fun RouteList(rows: ImmutableList<RouteRow>, onRouteClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val style = MapPlatformStyle.current.routeList
    val card = if (style.rowsOnCard) {
        Modifier.clip(AppTheme.platformStyle.cardShape).background(ListGroupDefaultColors.translucent().containerColor)
    } else {
        Modifier.fillMaxHeight()
    }
    LazyColumn(
        modifier.fillMaxWidth().then(card),
        contentPadding = PaddingValues(if (style.rowsOnCard) Grid.d1 else Grid.d0),
        verticalArrangement = Arrangement.spacedBy(Grid.d1),
    ) {
        items(rows, key = { it.id }) { row ->
            RouteRowItem(row = row, onClick = { onRouteClick(row.id) })
        }
    }
}

// The message above the fastest route, with the button that picks the smallest chip fitting the closest fun route, when there is one
@Composable
private fun NoFunRouteNote(message: NoFunRouteMessage, onUseBudget: (Budget) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        Text(
            text = message.text.localized(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        message.useBudget?.let { useBudget ->
            AppButton(
                text = useBudget.label.localized(),
                onClick = { onUseBudget(useBudget.budget) },
                colors = ButtonDefaultColors.tonal(),
                style = ButtonDefaultStyles.medium(),
            )
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { selected = row.isSelected }
            .clip(shape)
            .background(if (row.isSelected) selectedFill else Color.Transparent)
            .then(if (row.isSelected) Modifier.border(Grid.d0_5, color, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Grid.d4),
    ) {
        RouteRowContent(row = row, showsFacts = true)
    }
}

// The badge, the extra over the fastest route with the time and distance, the facts about the roads when shown, and the fun meter.
// A route without an extra, such as a saved route, shows no first line.
@Composable
internal fun RouteRowContent(row: RouteRow, showsFacts: Boolean, modifier: Modifier = Modifier, showsExtra: Boolean = true) {
    val color = routeColor(row.number)
    val badgeDescription = row.number?.let { stringResource(MR.strings.route_badge, it) }
        ?: stringResource(MR.strings.route_badge_fastest)
    Row(
        modifier = modifier.fillMaxWidth(),
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
            if (showsExtra) {
                Text(
                    text = row.extra?.localized() ?: stringResource(MR.strings.planning_fastest),
                    style = MaterialTheme.typography.titleMedium.withTabularFigures(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "${row.duration.localized()} · ${row.distance.localized()}",
                style = MaterialTheme.typography.bodyMedium.withTabularFigures(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (showsFacts) {
                Text(
                    text = stringResource(MR.strings.planning_route_facts, row.curvyDistance.localized(), row.ascent.localized()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningViewState()),
        viewState = RoutePlanningPreviewStates.planningViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlanningIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningViewState()),
        viewState = RoutePlanningPreviewStates.planningViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedViewState()),
        viewState = RoutePlanningPreviewStates.plannedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedViewState()),
        viewState = RoutePlanningPreviewStates.plannedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedDistancePreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedDistanceViewState()),
        viewState = RoutePlanningPreviewStates.plannedDistanceViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedDistanceIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedDistanceViewState()),
        viewState = RoutePlanningPreviewStates.plannedDistanceViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedMilesPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedMilesViewState()),
        viewState = RoutePlanningPreviewStates.plannedMilesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedFarStartPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedFarStartViewState()),
        viewState = RoutePlanningPreviewStates.plannedFarStartViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningPlannedFarStartIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedFarStartViewState()),
        viewState = RoutePlanningPreviewStates.plannedFarStartViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNoFunRoutePreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.noFunRouteViewState()),
        viewState = RoutePlanningPreviewStates.noFunRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNoFunRouteIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.noFunRouteViewState()),
        viewState = RoutePlanningPreviewStates.noFunRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNoFunRouteBeyondPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.noFunRouteBeyondViewState()),
        viewState = RoutePlanningPreviewStates.noFunRouteBeyondViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNoFunRouteBeyondIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.noFunRouteBeyondViewState()),
        viewState = RoutePlanningPreviewStates.noFunRouteBeyondViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNotCoveredPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningNotCoveredViewState()),
        viewState = RoutePlanningPreviewStates.planningNotCoveredViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningNotCoveredIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningNotCoveredViewState()),
        viewState = RoutePlanningPreviewStates.planningNotCoveredViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningOfflinePreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningOfflineViewState()),
        viewState = RoutePlanningPreviewStates.planningOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningOfflineViewState()),
        viewState = RoutePlanningPreviewStates.planningOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningFailedPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningFailedViewState()),
        viewState = RoutePlanningPreviewStates.planningFailedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningFailedViewState()),
        viewState = RoutePlanningPreviewStates.planningFailedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningCollapsedPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedCollapsedViewState()),
        viewState = RoutePlanningPreviewStates.plannedCollapsedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningCollapsedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedCollapsedViewState()),
        viewState = RoutePlanningPreviewStates.plannedCollapsedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningCollapsedPlanningPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningCollapsedViewState()),
        viewState = RoutePlanningPreviewStates.planningCollapsedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RoutePlanningCollapsedPlanningIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.planningCollapsedViewState()),
        viewState = RoutePlanningPreviewStates.planningCollapsedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
