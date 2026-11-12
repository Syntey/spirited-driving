package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.CloseButton
import com.syntey.spiriteddriving.core.ui.components.GroupPosition
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListRowIcon
import com.syntey.spiriteddriving.core.ui.components.ListGroupDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.MessageHost
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.components.showMessage
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
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
        // Always there below the top part, so pulling the sheet up reveals it as it grows
        SearchResults(
            viewState = viewState,
            onSuggestionClick = actions::onSuggestionClick,
            onSearchRetry = actions::onSearchRetry,
            modifier = Modifier.weight(1f),
        )
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
            isLoading = viewState.search.showsProgress,
            modifier = Modifier.weight(1f),
        )
        if (isOpen && !style.closesSearchWithBackArrow) CloseButton(action = close)
    }
}

/** What the sheet shows below the search field: the hint, a state that explains why there are no places, or the places. */
@Composable
private fun SearchResults(
    viewState: TripPlanViewState,
    onSuggestionClick: (PlaceSuggestion) -> Unit,
    onSearchRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val search = viewState.search
    when {
        search.showsHint -> Text(
            text = stringResource(MR.strings.search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth().padding(horizontal = Grid.d6, vertical = Grid.d4),
        )

        search.status == SearchStatus.Offline -> StateMessage(
            icon = painterResource(MR.images.offline),
            title = stringResource(MR.strings.search_offline_title),
            message = stringResource(MR.strings.search_offline_text),
            retry = retryAction(onSearchRetry),
            modifier = modifier.fillMaxWidth().imePadding(),
        )

        search.status == SearchStatus.Failed -> StateMessage(
            icon = painterResource(MR.images.warning),
            title = stringResource(MR.strings.search_failed_title),
            message = stringResource(MR.strings.search_failed_text),
            retry = retryAction(onSearchRetry),
            modifier = modifier.fillMaxWidth().imePadding(),
        )

        search.showsNoResults -> StateMessage(
            icon = painterResource(MR.images.search),
            title = stringResource(MR.strings.search_no_results_title, search.text.trim()),
            message = stringResource(MR.strings.search_no_results_text),
            modifier = modifier.fillMaxWidth().imePadding(),
        )

        else -> SuggestionList(
            rows = viewState.suggestionRows,
            onSuggestionClick = onSuggestionClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun retryAction(onRetry: () -> Unit): IconAction = IconAction(
    icon = painterResource(MR.images.refresh),
    label = stringResource(MR.strings.generic_try_again),
    onClick = onRetry,
)

@Composable
private fun SuggestionList(
    rows: ImmutableList<SuggestionRow>,
    onSuggestionClick: (PlaceSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    // Scrolling the list hides the keyboard, so it doesn't cover the places
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) focusManager.clearFocus()
    }
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth().imePadding()) {
        itemsIndexed(rows) { index, row ->
            ListRow(
                headline = boldHeadline(row),
                supporting = supportingText(row),
                leading = { ListRowIcon(icon = painterResource(row.suggestion.kind.icon)) },
                onClick = { onSuggestionClick(row.suggestion) },
                position = GroupPosition.of(index, rows.size),
                groupColors = ListGroupDefaultColors.translucent(),
                isDimmed = !row.suggestion.isCovered,
                tabularSupporting = true,
                contentDescription = accessibilityText(row),
            )
        }
    }
}

// The matched parts of the name in semibold; the ranges end exclusively, as `until` makes them
private fun boldHeadline(row: SuggestionRow): AnnotatedString = buildAnnotatedString {
    append(row.suggestion.place.name)
    row.boldRanges.forEach { range ->
        addStyle(SpanStyle(fontWeight = FontWeight.SemiBold), range.first, range.last + 1)
    }
}

// A place outside the covered area says so in place of its distance, which routes can't reach anyway
@Composable
private fun supportingText(row: SuggestionRow): String? {
    val first = if (row.suggestion.isCovered) row.distance else stringResource(MR.strings.search_not_covered)
    return listOfNotNull(first, row.suggestion.place.area).joinToString(" · ").takeIf { it.isNotEmpty() }
}

// Screen readers get the name, kind, distance and area as one sentence, and "not covered yet" for a place outside the area
@Composable
private fun accessibilityText(row: SuggestionRow): String {
    val suggestion = row.suggestion
    val kind = stringResource(suggestion.kind.word)
    val distance = row.distance.takeIf { suggestion.isCovered }
    val notCovered = if (suggestion.isCovered) null else stringResource(MR.strings.search_not_covered_a11y)
    return listOfNotNull(suggestion.place.name, kind, distance, suggestion.place.area, notCovered).joinToString(", ")
}

private val PlaceKind.icon: ImageResource
    get() = when (this) {
        PlaceKind.Town -> MR.images.place_town
        PlaceKind.Street -> MR.images.place_street
        PlaceKind.Address -> MR.images.place_address
        PlaceKind.Nature -> MR.images.place_nature
        PlaceKind.PointOfInterest -> MR.images.place_poi
    }

private val PlaceKind.word: StringResource
    get() = when (this) {
        PlaceKind.Town -> MR.strings.search_kind_town
        PlaceKind.Street -> MR.strings.search_kind_street
        PlaceKind.Address -> MR.strings.search_kind_address
        PlaceKind.Nature -> MR.strings.search_kind_nature
        PlaceKind.PointOfInterest -> MR.strings.search_kind_poi
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
