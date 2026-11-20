package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.model.ui.MapSheetPreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.ui.components.MapPlatformStyle
import com.syntey.spiriteddriving.feature.map.ui.components.SearchResults
import com.syntey.spiriteddriving.feature.map.ui.components.SearchRow
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The place search step of the map's sheet: the field, the notes above it while collapsed, the start's destination and the
 * results. The destination search and the start search both show it.
 */
@Composable
internal fun PlaceSearchHeader(viewState: MapSheetViewState, actions: MapSheetScreen.Actions) {
    if (viewState.startSearch != null) {
        StartStepTo(destination = viewState.planning?.destination)
    } else if (viewState.sheetHeight == SheetHeight.Collapsed) {
        CollapsedNotes(viewState, actions)
    }
    SearchRow(viewState = viewState, actions = actions)
}

// Always there below the top part, so pulling the sheet up reveals it as it grows
@Composable
internal fun PlaceSearchResults(viewState: MapSheetViewState, actions: MapSheetScreen.Actions, modifier: Modifier = Modifier) {
    SearchResults(
        viewState = viewState,
        onSuggestionClick = actions::onSuggestionClick,
        onRecentPlaceClick = actions::onRecentPlaceClick,
        onRecentPlaceRemove = actions::onRecentPlaceRemove,
        onSearchRetry = actions::onSearchRetry,
        myLocationRow = viewState.myLocationRow,
        onMyLocationClick = actions::onMyLocationClick,
        onSavedRoutesClick = actions::onSavedRoutesClick,
        modifier = modifier,
    )
}

// The notes above the search field, shown while the sheet is collapsed
@Composable
private fun CollapsedNotes(viewState: MapSheetViewState, actions: MapSheetScreen.Actions) {
    val style = MapPlatformStyle.current
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

// The destination above the start step's field, so the user knows which trip the start is for
@Composable
private fun StartStepTo(destination: Place?) {
    Text(
        text = stringResource(MR.strings.search_start_to, destination?.name ?: stringResource(MR.strings.search_point_on_map)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.searchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.searchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchEmptyPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.viewState().copy(sheetHeight = SheetHeight.Half),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchEmptyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.viewState().copy(sheetHeight = SheetHeight.Half),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchOfflinePreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.offlineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.offlineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchFailedPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.failedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.failedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchNoResultsPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.noResultsViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchNoResultsIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.noResultsViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchSlowPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.slowSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchSlowIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.slowSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchDistancesPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.searchDistancesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetSearchDistancesIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.searchDistancesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetStartPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.startStepViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetStartIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.startStepViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetStartApproximatePreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.startStepViewState(locationAccess = LocationAccess.Approximate),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetStartApproximateIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.startStepViewState(locationAccess = LocationAccess.Approximate),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetStartNoAccessPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.startStepViewState(locationAccess = null),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetStartNoAccessIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.startStepViewState(locationAccess = null),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetRecentPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.recentViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetRecentIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.recentViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetRecentSearchPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.recentSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetRecentSearchIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.recentSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetRecentOfflinePreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.recentOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun MapSheetRecentOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.recentOfflineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
