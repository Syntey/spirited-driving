package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.MessageHost
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.showMessage
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningViewState
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.CollapsedRoutePlanning
import com.syntey.spiriteddriving.feature.map.ui.components.MapPlatformStyle
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingLayout
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningBody
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningHeader
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteLayout
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchHeader
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchResults
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.MapSheetPreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningPreviewStates
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

/**
 * The content of the map's sheet, shared by both platforms: the Android bottom sheet and the native iOS sheet host it. It
 * fills the sheet. [onCollapsedHeightChange] gets the sheet's collapsed height: that of the notes and the search field, while
 * planning that of the collapsed planning layout, and in the rating step that of its header and thumbs.
 */
@Composable
fun MapSheetUi(screen: MapSheetScreen, onCollapsedHeightChange: (Dp) -> Unit, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val activeStep by screen.activeStep.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notCoveredMessage = stringResource(MR.strings.search_not_covered_message)
    val saveOfflineMessage = stringResource(MR.strings.saved_routes_offline_saving)
    val saveFailedMessage = stringResource(MR.strings.saved_routes_save_failed)
    val removeOfflineMessage = stringResource(MR.strings.saved_routes_offline_changes)
    val removeFailedMessage = stringResource(MR.strings.saved_routes_remove_failed)
    EventsEffect(screen.events) {
        onEvent<MapSheetUiEvent.NotCovered> { scope.launch { hostState.showMessage(notCoveredMessage) } }
        onEvent<MapSheetUiEvent.SaveRouteOffline> { scope.launch { hostState.showMessage(saveOfflineMessage) } }
        onEvent<MapSheetUiEvent.SaveRouteFailed> { scope.launch { hostState.showMessage(saveFailedMessage) } }
        onEvent<MapSheetUiEvent.RemoveRouteOffline> { scope.launch { hostState.showMessage(removeOfflineMessage) } }
        onEvent<MapSheetUiEvent.RemoveRouteFailed> { scope.launch { hostState.showMessage(removeFailedMessage) } }
    }
    Box(modifier) {
        Content(
            viewState = viewState,
            step = activeStep,
            actions = screen.actions,
            onCollapsedHeightChange = onCollapsedHeightChange,
            modifier = Modifier.fillMaxSize(),
        )
        MessageHost(hostState, Modifier.align(Alignment.BottomCenter).imePadding())
    }
}

@Composable
internal fun Content(
    viewState: MapSheetViewState,
    step: ActiveSheetStep,
    actions: MapSheetScreen.Actions,
    onCollapsedHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    // Read by the size callbacks, which aren't recomposed with the sheet
    val currentSheetHeight by rememberUpdatedState(viewState.sheetHeight)
    val planningScreen = (step as? ActiveSheetStep.RoutePlanning)?.screen
    val planningState = planningScreen?.viewState?.collectAsStateWithLifecycle()?.value
    val planningActions = planningScreen?.actions
    // The start search replaces the planning layout until it closes; the planning height stays measured while planning
    val planning = planningState?.takeIf { it.startSearch == null }
    val isPlanning = planningState != null
    val currentIsPlanning by rememberUpdatedState(isPlanning)
    val reportCollapsedHeight by rememberUpdatedState(onCollapsedHeightChange)
    val collapsedHeights = remember { CollapsedHeights() }
    val shownHeight = collapsedHeights.shownBy(step)
    LaunchedEffect(shownHeight) { shownHeight?.let(reportCollapsedHeight) }
    // The keyboard comes with the full height: dragging the sheet lower or closing the search hides it
    LaunchedEffect(viewState.sheetHeight) {
        if (viewState.sheetHeight != SheetHeight.Full) focusManager.clearFocus()
    }
    Column(modifier.fillMaxSize()) {
        MeasuredPlanning(
            planning = planningState,
            planningActions = planningActions,
            viewState = viewState,
            actions = actions,
            onCollapsedHeight = { collapsedHeights.planning = it },
        )
        if (step is ActiveSheetStep.Rating) {
            RatingLayout(screen = step.screen, sheetHeight = viewState.sheetHeight, onCollapsedHeight = { collapsedHeights.rating = it })
        } else if (step is ActiveSheetStep.OneRoute) {
            OneRouteLayout(screen = step.screen, viewState = viewState, actions = actions, onCollapsedHeight = {
                collapsedHeights.oneRoute =
                    it
            })
        } else if (planning != null && planningActions != null && viewState.sheetHeight == SheetHeight.Collapsed) {
            CollapsedRoutePlanning(planning = planning, planningActions = planningActions, viewState = viewState, actions = actions)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Unbounded, so the top part reports the height it needs even while the sheet is still shorter
                    .wrapContentHeight(align = Alignment.Top, unbounded = true)
                    // The notes hide above collapsed, which makes the top part shorter there: only the collapsed layout reports
                    // its height, or the sheet's collapsed height would shrink along with the notes. Planning reports its own.
                    .onSizeChanged { size ->
                        if (currentSheetHeight == SheetHeight.Collapsed && !currentIsPlanning) {
                            collapsedHeights.search = with(density) { size.height.toDp() }
                        }
                    }
                    .padding(style.sheetContentPadding),
                verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
            ) {
                if (planning != null && planningActions != null) {
                    RoutePlanningHeader(planning = planning.planning, planningActions = planningActions)
                } else {
                    PlaceSearchHeader(
                        viewState = viewState,
                        actions = actions,
                        tripDestination = planningState?.planning?.destination,
                    )
                }
            }
            if (planning != null && planningActions != null) {
                RoutePlanningBody(
                    planning = planning,
                    planningActions = planningActions,
                    viewState = viewState,
                    actions = actions,
                    modifier = Modifier.weight(1f),
                )
            } else {
                PlaceSearchResults(viewState = viewState, actions = actions, modifier = Modifier.weight(1f))
            }
        }
    }
}

// Planning's collapsed layout, measured unplaced while planning, so its height is known before the sheet gets there
@Composable
private fun MeasuredPlanning(
    planning: RoutePlanningViewState?,
    planningActions: RoutePlanningScreen.Actions?,
    viewState: MapSheetViewState,
    actions: MapSheetScreen.Actions,
    onCollapsedHeight: (Dp) -> Unit,
) {
    if (planning == null || planningActions == null) return
    val density = LocalDensity.current
    CollapsedRoutePlanning(
        planning = planning,
        planningActions = planningActions,
        viewState = viewState,
        actions = actions,
        modifier = Modifier
            .measureOnly { height -> onCollapsedHeight(with(density) { height.toDp() }) }
            .clearAndSetSemantics {},
    )
}

// The collapsed height of each layout, measured as the layout is shown. The sheet's collapsed detent follows the one on screen,
// so starting or cancelling planning, or ending a step, moves the sheet to the right one at once.
private class CollapsedHeights {
    var search by mutableStateOf<Dp?>(null)
    var planning by mutableStateOf<Dp?>(null)
    var rating by mutableStateOf<Dp?>(null)
    var oneRoute by mutableStateOf<Dp?>(null)

    // The rating step takes the place of the others, and a saved route's step that of search and planning
    fun shownBy(step: ActiveSheetStep): Dp? = when (step) {
        is ActiveSheetStep.Rating -> rating
        is ActiveSheetStep.OneRoute -> oneRoute
        is ActiveSheetStep.RoutePlanning -> planning
        ActiveSheetStep.Search -> search
    }
}

// Measures the content at the height it needs without placing it, so the collapsed sheet's height is known before the
// sheet gets there. Unplaced, it isn't drawn and takes no space.
internal fun Modifier.measureOnly(onHeight: (Int) -> Unit): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    onHeight(placeable.height)
    layout(0, 0) {}
}

@ComponentPreviews
@Composable
private fun MapSheetPreview() = Showcase {
    Content(viewState = MapSheetViewState(), step = ActiveSheetStep.Search, actions = noOpActions(), onCollapsedHeightChange = {
    }, modifier = Modifier.height(Grid.d100))
}

@ComponentPreviews
@Composable
private fun MapSheetIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = MapSheetViewState(), step = ActiveSheetStep.Search, actions = noOpActions(), onCollapsedHeightChange = {
    }, modifier = Modifier.height(Grid.d100))
}

@ComponentPreviews
@Composable
private fun MapSheetPreciseLocationPreview() = Showcase {
    Content(
        step = ActiveSheetStep.Search,
        viewState = MapSheetPreviewStates.approximateViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetPreciseLocationIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = ActiveSheetStep.Search,
        viewState = MapSheetPreviewStates.approximateViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetOutsidePreview() = Showcase {
    Content(
        step = ActiveSheetStep.Search,
        viewState = MapSheetPreviewStates.outsideViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetOutsideIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = ActiveSheetStep.Search,
        viewState = MapSheetPreviewStates.outsideViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetPlannedFromPickedStartPreview() = Showcase {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedFromPickedStartViewState()),
        viewState = RoutePlanningPreviewStates.plannedFromPickedStartViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetPlannedFromPickedStartIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        step = RoutePlanningPreviewStates.stepOf(RoutePlanningPreviewStates.plannedFromPickedStartViewState()),
        viewState = RoutePlanningPreviewStates.plannedFromPickedStartViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
