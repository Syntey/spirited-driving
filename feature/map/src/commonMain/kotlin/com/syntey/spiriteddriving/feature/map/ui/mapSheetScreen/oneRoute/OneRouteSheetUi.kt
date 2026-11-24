package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.measureOnly
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
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
import com.syntey.spiriteddriving.feature.map.model.ui.OneRoutePreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.ui.components.MapPlatformStyle
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.Content
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.ActiveSheetStep
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.PlanningProgress
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningActions
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RouteRowContent
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource

/** The top of a saved route's step: its name, the start's area under it and a close button. The start can't be changed. */
@Composable
internal fun OneRouteHeader(oneRoute: OneRouteViewState, oneRouteActions: OneRouteScreen.Actions, modifier: Modifier = Modifier) {
    SheetHeader(
        title = oneRoute.title,
        subtitle = oneRoute.subtitle?.localized(),
        close = IconAction(
            icon = painterResource(MR.images.close),
            label = stringResource(MR.strings.generic_close),
            onClick = oneRouteActions::onCloseClick,
        ),
        modifier = modifier,
    )
}

/**
 * Everything below the header: the summary, the note about the start, then Save and Drive with the attribution. It scrolls
 * when the sheet is shorter than its content.
 */
@Composable
internal fun OneRouteBody(
    oneRoute: OneRouteViewState,
    oneRouteActions: OneRouteScreen.Actions,
    viewState: MapSheetViewState,
    actions: MapSheetScreen.Actions,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        OneRouteSummary(oneRoute = oneRoute, oneRouteActions = oneRouteActions, isCollapsed = false)
        OneRouteStartNote(oneRoute = oneRoute, locationAccess = viewState.locationAccess)
        RoutePlanningActions(viewState = viewState, actions = actions)
    }
}

/** The header, the summary, then Save and Drive: the collapsed sheet's layout of a saved route. */
@Composable
internal fun CollapsedOneRoute(
    oneRoute: OneRouteViewState,
    oneRouteActions: OneRouteScreen.Actions,
    viewState: MapSheetViewState,
    actions: MapSheetScreen.Actions,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().padding(style.sheetContentPadding),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        OneRouteHeader(oneRoute = oneRoute, oneRouteActions = oneRouteActions)
        OneRouteSummary(oneRoute = oneRoute, oneRouteActions = oneRouteActions, isCollapsed = true)
        RoutePlanningActions(viewState = viewState, actions = actions)
    }
}

// The route's facts once loaded, the planning progress while it loads, and why it can't load, with Try again where it helps
@Composable
private fun OneRouteSummary(
    oneRoute: OneRouteViewState,
    oneRouteActions: OneRouteScreen.Actions,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        when (oneRoute.step.status) {
            OneRouteStatus.Loading -> PlanningProgress()

            OneRouteStatus.Ready -> oneRoute.row?.let { row ->
                RouteRowContent(row = row, showsFacts = !isCollapsed, showsExtra = false)
            }

            OneRouteStatus.Offline -> OneRouteFailure(
                icon = MR.images.offline,
                title = MR.strings.planning_offline_title,
                message = MR.strings.one_route_offline_text,
                isCollapsed = isCollapsed,
                oneRouteActions = oneRouteActions,
            )

            OneRouteStatus.Failed -> OneRouteFailure(
                icon = MR.images.warning,
                title = MR.strings.planning_failed_title,
                message = MR.strings.planning_failed_text,
                isCollapsed = isCollapsed,
                oneRouteActions = oneRouteActions,
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
    oneRouteActions: OneRouteScreen.Actions,
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
                onClick = oneRouteActions::onRetryClick,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// Without location access the note says where the route starts; with it, the start's distance comes with the actions
@Composable
private fun OneRouteStartNote(oneRoute: OneRouteViewState, locationAccess: LocationAccess?) {
    val start = oneRoute.step.origin.start.shortArea ?: return
    if (locationAccess.allowsLocation) return
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
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.savedStep())),
        viewState = OneRoutePreviewStates.oneRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteReadyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.savedStep())),
        viewState = OneRoutePreviewStates.oneRouteViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRoutePastDrivePreview() = Showcase {
    Content(
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.pastDriveStep())),
        viewState = OneRoutePreviewStates.pastDriveViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRoutePastDriveIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.pastDriveStep())),
        viewState = OneRoutePreviewStates.pastDriveViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteLoadingPreview() = Showcase {
    Content(
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.savedStep(status = OneRouteStatus.Loading))),
        viewState = OneRoutePreviewStates.oneRouteViewState(status = OneRouteStatus.Loading),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteOfflinePreview() = Showcase {
    Content(
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.savedStep(status = OneRouteStatus.Offline))),
        viewState = OneRoutePreviewStates.oneRouteViewState(status = OneRouteStatus.Offline),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun OneRouteCollapsedPreview() = Showcase {
    Content(
        step = ActiveSheetStep.OneRoute(OneRoutePreviewStates.screen(OneRoutePreviewStates.savedStep())),
        viewState = OneRoutePreviewStates.oneRouteViewState(sheetHeight = SheetHeight.Collapsed),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

// A saved route's step replaces the search and planning, and is measured the same way as the rating step
@Composable
internal fun ColumnScope.OneRouteLayout(
    screen: OneRouteScreen,
    viewState: MapSheetViewState,
    actions: MapSheetScreen.Actions,
    onCollapsedHeight: (Dp) -> Unit,
) {
    val oneRoute by screen.viewState.collectAsStateWithLifecycle()
    val oneRouteActions = screen.actions
    val style = MapPlatformStyle.current
    val density = LocalDensity.current
    CollapsedOneRoute(
        oneRoute = oneRoute,
        oneRouteActions = oneRouteActions,
        viewState = viewState,
        actions = actions,
        modifier = Modifier
            .measureOnly { height -> onCollapsedHeight(with(density) { height.toDp() }) }
            .clearAndSetSemantics {},
    )
    if (viewState.sheetHeight == SheetHeight.Collapsed) {
        CollapsedOneRoute(oneRoute = oneRoute, oneRouteActions = oneRouteActions, viewState = viewState, actions = actions)
    } else {
        Column(
            Modifier.fillMaxSize().padding(style.sheetContentPadding),
            verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
        ) {
            OneRouteHeader(oneRoute = oneRoute, oneRouteActions = oneRouteActions)
            OneRouteBody(
                oneRoute = oneRoute,
                oneRouteActions = oneRouteActions,
                viewState = viewState,
                actions = actions,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
