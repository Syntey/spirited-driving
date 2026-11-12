package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.CloseButton
import com.syntey.spiriteddriving.core.ui.components.GroupPosition
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroupDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The content of the map's sheet, shared by both platforms: the Android bottom sheet and the native iOS sheet host it. It
 * fills the sheet. [onCollapsedHeightChange] gets the height of its top part, the notes and the search field, which is the
 * sheet's collapsed height.
 */
@Composable
fun TripPlanSheetUi(screen: TripPlanScreen, onCollapsedHeightChange: (Dp) -> Unit, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, onCollapsedHeightChange = onCollapsedHeightChange, modifier = modifier)
}

@Composable
private fun Content(
    viewState: TripPlanViewState,
    actions: TripPlanScreen.Actions,
    onCollapsedHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
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
                .onSizeChanged { size -> onCollapsedHeightChange(with(density) { size.height.toDp() }) }
                .padding(style.sheetContentPadding),
            verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
        ) {
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
            SearchRow(viewState = viewState, actions = actions)
        }
        // Always there below the top part, so pulling the sheet up reveals it as it grows
        SearchResults(search = viewState.search, onSuggestionClick = actions::onSuggestionClick, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SearchRow(viewState: TripPlanViewState, actions: TripPlanScreen.Actions) {
    val style = MapPlatformStyle.current
    val isOpen = viewState.sheetHeight != SheetHeight.Collapsed
    val close = IconAction(
        icon = painterResource(if (style.closesSearchWithBackArrow) MR.images.back else MR.images.close),
        label = stringResource(MR.strings.generic_close),
        onClick = actions::onSearchClose,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
        SearchField(
            text = viewState.search.text,
            onTextChange = actions::onSearchTextChange,
            placeholder = stringResource(MR.strings.map_search_placeholder),
            icon = painterResource(MR.images.search),
            clear = IconAction(
                icon = painterResource(MR.images.close),
                label = stringResource(MR.strings.search_clear),
                onClick = actions::onSearchClear,
            ),
            onFocus = actions::onSearchFocus,
            back = close.takeIf { isOpen && style.closesSearchWithBackArrow },
            modifier = Modifier.weight(1f),
        )
        if (isOpen && !style.closesSearchWithBackArrow) CloseButton(action = close)
    }
}

@Composable
private fun SearchResults(search: SearchState, onSuggestionClick: (PlaceSuggestion) -> Unit, modifier: Modifier = Modifier) {
    if (search.showsHint) {
        Text(
            text = stringResource(MR.strings.search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth().padding(horizontal = Grid.d6, vertical = Grid.d4),
        )
        return
    }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    // Scrolling the list hides the keyboard, so it doesn't cover the places
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) focusManager.clearFocus()
    }
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth().imePadding()) {
        itemsIndexed(search.suggestions) { index, suggestion ->
            ListRow(
                headline = suggestion.place.name,
                supporting = suggestion.place.area,
                onClick = { onSuggestionClick(suggestion) },
                position = GroupPosition.of(index, search.suggestions.size),
                groupColors = ListGroupDefaultColors.translucent(),
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
        viewState = TripPlanScreenPreviews.approximateViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreciseLocationIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.approximateViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetOutsidePreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.outsideViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetOutsideIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.outsideViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchPreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.searchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.searchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchEmptyPreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.viewState().copy(sheetHeight = SheetHeight.Half),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchEmptyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.viewState().copy(sheetHeight = SheetHeight.Half),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
