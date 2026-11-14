package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.MessageHost
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.showMessage
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.ui.components.MapPlatformStyle
import com.syntey.spiriteddriving.feature.map.ui.components.RoutePlanningBody
import com.syntey.spiriteddriving.feature.map.ui.components.RoutePlanningHeader
import com.syntey.spiriteddriving.feature.map.ui.components.SearchResults
import com.syntey.spiriteddriving.feature.map.ui.components.SearchRow
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

/**
 * The content of the map's sheet, shared by both platforms: the Android bottom sheet and the native iOS sheet host it. It
 * fills the sheet. [onCollapsedHeightChange] gets the height of its top part, the notes and the search field, while the
 * sheet is collapsed: that's the sheet's collapsed height.
 */
@Composable
fun TripPlanSheetUi(screen: TripPlanScreen, onCollapsedHeightChange: (Dp) -> Unit, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notCoveredMessage = stringResource(MR.strings.search_not_covered_message)
    EventsEffect(screen.events) {
        onEvent<TripPlanUiEvent.NotCovered> { scope.launch { hostState.showMessage(notCoveredMessage) } }
    }
    Box(modifier) {
        Content(
            viewState = viewState,
            actions = screen.actions,
            onCollapsedHeightChange = onCollapsedHeightChange,
            modifier = Modifier.fillMaxSize(),
        )
        MessageHost(hostState, Modifier.align(Alignment.BottomCenter).imePadding())
    }
}

@Composable
internal fun Content(
    viewState: TripPlanViewState,
    actions: TripPlanScreen.Actions,
    onCollapsedHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    // Read by the size callback, which isn't recomposed with the sheet
    val currentSheetHeight by rememberUpdatedState(viewState.sheetHeight)
    // The keyboard comes with the full height: dragging the sheet lower or closing the search hides it
    LaunchedEffect(viewState.sheetHeight) {
        if (viewState.sheetHeight != SheetHeight.Full) focusManager.clearFocus()
    }
    Column(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Unbounded, so the top part reports the height it needs even while the sheet is still shorter
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                // The notes hide above collapsed, which makes the top part shorter there: only the collapsed layout reports
                // its height, or the sheet's collapsed height would shrink along with the notes
                .onSizeChanged { size ->
                    if (currentSheetHeight == SheetHeight.Collapsed) onCollapsedHeightChange(with(density) { size.height.toDp() })
                }
                .padding(style.sheetContentPadding),
            verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
        ) {
            val planning = viewState.planning
            if (planning != null) {
                RoutePlanningHeader(destination = planning.destination, actions = actions)
            } else {
                if (viewState.sheetHeight == SheetHeight.Collapsed) {
                    if (viewState.map.isOutsideCoveredArea) {
                        NoteCard(
                            title = stringResource(MR.strings.map_outside_title),
                            text = stringResource(MR.strings.map_outside_text),
                            icon = painterResource(MR.images.map),
                        )
                    }
                    if (viewState.showsPreciseLocationNote) {
                        NoteCard(
                            title = stringResource(MR.strings.map_precise_title),
                            text = stringResource(MR.strings.map_precise_text),
                            icon = painterResource(style.preciseLocationIcon),
                            actionLabel = stringResource(MR.strings.map_precise_action),
                            onAction = actions::onPreciseLocationClick,
                            close = IconAction(
                                icon = painterResource(MR.images.close),
                                label = stringResource(MR.strings.generic_close),
                                onClick = actions::onPreciseLocationNoteClose,
                            ),
                        )
                    }
                }
                SearchRow(viewState = viewState, actions = actions)
            }
        }
        if (viewState.planning != null) {
            RoutePlanningBody(viewState = viewState, actions = actions, modifier = Modifier.weight(1f))
        } else {
            // Always there below the top part, so pulling the sheet up reveals it as it grows
            SearchResults(
                viewState = viewState,
                onSuggestionClick = actions::onSuggestionClick,
                onRecentPlaceClick = actions::onRecentPlaceClick,
                onRecentPlaceRemove = actions::onRecentPlaceRemove,
                onSearchRetry = actions::onSearchRetry,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreview() = Showcase {
    Content(viewState = TripPlanViewState(), actions = noOpActions(), onCollapsedHeightChange = {}, modifier = Modifier.height(Grid.d100))
}

@ComponentPreviews
@Composable
private fun TripPlanSheetIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = TripPlanViewState(), actions = noOpActions(), onCollapsedHeightChange = {}, modifier = Modifier.height(Grid.d100))
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreciseLocationPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.approximateViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreciseLocationIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.approximateViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetOutsidePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.outsideViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetOutsideIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.outsideViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.searchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.searchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchEmptyPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.viewState().copy(sheetHeight = SheetHeight.Half),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchEmptyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.viewState().copy(sheetHeight = SheetHeight.Half),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchOfflinePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.offlineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.offlineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchFailedPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.failedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.failedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchNoResultsPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.noResultsViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchNoResultsIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.noResultsViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchSlowPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.slowSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchSlowIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.slowSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchDistancesPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.searchDistancesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchDistancesIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.searchDistancesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetRecentPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.recentViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetRecentIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.recentViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetRecentSearchPreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.recentSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetRecentSearchIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.recentSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetRecentOfflinePreview() = Showcase {
    Content(
        viewState = TripPlanPreviewStates.recentOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetRecentOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanPreviewStates.recentOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
