package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.GroupPosition
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroupDefaultColors
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.components.ListHeader
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.ListRowIcon
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.RecentPlaceRow
import com.syntey.spiriteddriving.feature.map.model.ui.SearchFailure
import com.syntey.spiriteddriving.feature.map.model.ui.SuggestionRow
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList

/** What the sheet shows below the search field: the hint, a state that explains why there are no places, or the places. */
@Composable
internal fun SearchResults(
    viewState: TripPlanViewState,
    onSuggestionClick: (PlaceSuggestion) -> Unit,
    onRecentPlaceClick: (Place) -> Unit,
    onRecentPlaceRemove: (Place) -> Unit,
    onSearchRetry: () -> Unit,
    myLocationRow: MyLocationRow?,
    onMyLocationClick: () -> Unit,
    onSavedRoutesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val search = viewState.search
    val recentRows = viewState.recentRows
    // The Saved routes row or the My location row above it: the hint goes below the row in the list
    val savedRoutesText = if (viewState.showsSavedRoutesRow) viewState.savedRoutesRowText.localized() else null
    when {
        search.showsHint && myLocationRow == null && savedRoutesText == null -> SearchHint(modifier = modifier.fillMaxWidth())

        search.showsFailureAlone -> search.failure?.let { failure ->
            FailureMessage(
                failure = failure,
                onSearchRetry = onSearchRetry,
                modifier = modifier.fillMaxWidth().imePadding(),
            )
        }

        search.showsNoResults -> StateMessage(
            icon = painterResource(MR.images.search),
            title = stringResource(MR.strings.search_no_results_title, search.text.trim()),
            message = stringResource(MR.strings.search_no_results_text),
            modifier = modifier.fillMaxWidth().imePadding(),
        )

        else -> PlaceList(
            recentRows = recentRows,
            suggestionRows = viewState.suggestionRows,
            showsHeader = search.showsRecentHeader,
            savedRoutesText = savedRoutesText,
            onSavedRoutesClick = onSavedRoutesClick,
            myLocationRow = myLocationRow,
            onMyLocationClick = onMyLocationClick,
            onRecentPlaceClick = onRecentPlaceClick,
            onRecentPlaceRemove = onRecentPlaceRemove,
            onSuggestionClick = onSuggestionClick,
            // The hint takes the failure's place below the list, as the hint only shows with no places to list. The failure
            // stays below the matching recent places, which stay while the search has failed.
            footer = if (search.showsHint) {
                { SearchHint(modifier = Modifier.fillMaxWidth()) }
            } else {
                search.failure?.let { failure ->
                    { FailureMessage(failure = failure, onSearchRetry = onSearchRetry, modifier = Modifier.fillMaxWidth()) }
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun SearchHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(MR.strings.search_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = Grid.d6, vertical = Grid.d4),
    )
}

@Composable
private fun FailureMessage(failure: SearchFailure, onSearchRetry: () -> Unit, modifier: Modifier = Modifier) {
    val retry = IconAction(
        icon = painterResource(MR.images.refresh),
        label = stringResource(MR.strings.generic_try_again),
        onClick = onSearchRetry,
    )
    StateMessage(
        icon = painterResource(failure.icon),
        title = stringResource(failure.title),
        message = stringResource(failure.text),
        retry = retry,
        modifier = modifier,
    )
}

// The recent places and the suggestions form one group, the recent ones first, under a "Recent" header while the field is blank
@Composable
private fun PlaceList(
    recentRows: ImmutableList<RecentPlaceRow>,
    suggestionRows: ImmutableList<SuggestionRow>,
    showsHeader: Boolean,
    savedRoutesText: String?,
    onSavedRoutesClick: () -> Unit,
    myLocationRow: MyLocationRow?,
    onMyLocationClick: () -> Unit,
    onRecentPlaceClick: (Place) -> Unit,
    onRecentPlaceRemove: (Place) -> Unit,
    onSuggestionClick: (PlaceSuggestion) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    // Scrolling the list hides the keyboard, so it doesn't cover the places
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) focusManager.clearFocus()
    }
    val count = recentRows.size + suggestionRows.size
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth().imePadding()) {
        // Its own group, so the Recent header can sit between it and the places
        if (savedRoutesText != null) {
            item { SavedRoutesItem(supporting = savedRoutesText, onClick = onSavedRoutesClick) }
        }
        if (myLocationRow != null) {
            item { MyLocationItem(row = myLocationRow, onClick = onMyLocationClick) }
        }
        if (showsHeader) {
            item { ListHeader(text = stringResource(MR.strings.search_recent_header), style = AppTheme.platformStyle.sheetList) }
        }
        // Keyed, so a swiped row's state doesn't carry over to the row that moves up into its place
        itemsIndexed(recentRows, key = { _, row -> row.place.toString() }) { index, row ->
            // A point picked on the map has no name: the row says what it is, in plain text
            val pointOnMap = stringResource(MR.strings.search_point_on_map)
            ListRow(
                headline = row.place.name?.let { boldHeadline(name = it, boldRanges = row.boldRanges) }
                    ?: AnnotatedString(pointOnMap),
                supporting = supportingText(isCovered = row.isCovered, distance = row.distance, area = row.place.area),
                leading = { ListRowIcon(icon = painterResource(MR.images.recent)) },
                onClick = { onRecentPlaceClick(row.place) },
                swipeDelete = IconAction(
                    icon = painterResource(MR.images.delete),
                    label = stringResource(MR.strings.search_recent_remove),
                    onClick = { onRecentPlaceRemove(row.place) },
                ),
                position = GroupPosition.of(index, count),
                groupColors = ListGroupDefaultColors.translucent(),
                style = AppTheme.platformStyle.sheetList,
                isDimmed = !row.isCovered,
                tabularSupporting = true,
                contentDescription = recentAccessibilityText(row),
            )
        }
        itemsIndexed(suggestionRows) { index, row ->
            ListRow(
                headline = boldHeadline(name = row.suggestion.place.name.orEmpty(), boldRanges = row.boldRanges),
                supporting = supportingText(
                    isCovered = row.suggestion.isCovered,
                    distance = row.distance,
                    area = row.suggestion.place.area,
                ),
                leading = { ListRowIcon(icon = painterResource(row.suggestion.kind.icon)) },
                onClick = { onSuggestionClick(row.suggestion) },
                position = GroupPosition.of(recentRows.size + index, count),
                groupColors = ListGroupDefaultColors.translucent(),
                style = AppTheme.platformStyle.sheetList,
                isDimmed = !row.suggestion.isCovered,
                tabularSupporting = true,
                contentDescription = accessibilityText(row),
            )
        }
        if (footer != null) {
            item { footer() }
        }
    }
}

// The matched parts of the name in semibold; the ranges end exclusively, as `until` makes them
private fun boldHeadline(name: String, boldRanges: List<IntRange>): AnnotatedString = buildAnnotatedString {
    append(name)
    boldRanges.forEach { range ->
        addStyle(SpanStyle(fontWeight = FontWeight.SemiBold), range.first, range.last + 1)
    }
}

// Saved routes, a group of its own above the recent places, as the My location row is in the start step
@Composable
private fun SavedRoutesItem(supporting: String, onClick: () -> Unit) {
    ListRow(
        headline = stringResource(MR.strings.saved_routes_title),
        supporting = supporting,
        leading = {
            ListRowIcon(
                icon = painterResource(MR.images.bookmark),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        onClick = onClick,
        chevron = painterResource(MR.images.chevron_right),
        groupColors = ListGroupDefaultColors.translucent(),
        style = AppTheme.platformStyle.sheetList,
    )
}

// The user's location as the start: its supporting line says why it's not usable yet, and it's dimmed outside the covered area
@Composable
private fun MyLocationItem(row: MyLocationRow, onClick: () -> Unit) {
    val headline = stringResource(MR.strings.search_my_location)
    val supporting = when (row) {
        MyLocationRow.Available -> null
        MyLocationRow.NeedsPreciseLocation -> stringResource(MR.strings.search_my_location_needs_precise)
        MyLocationRow.NeedsLocationAccess -> stringResource(MR.strings.search_my_location_needs_access)
        MyLocationRow.OutsideCoveredArea -> stringResource(MR.strings.search_my_location_outside)
    }
    ListRow(
        headline = headline,
        supporting = supporting,
        leading = { ListRowIcon(icon = painterResource(MR.images.locate_arrow)) },
        onClick = onClick,
        groupColors = ListGroupDefaultColors.translucent(),
        style = AppTheme.platformStyle.sheetList,
        isDimmed = row == MyLocationRow.OutsideCoveredArea,
        contentDescription = listOfNotNull(headline, supporting).joinToString(", "),
    )
}

// A place outside the covered area says so in place of its distance, which routes can't reach anyway
@Composable
private fun supportingText(isCovered: Boolean, distance: StringDesc?, area: String?): String? {
    val first = if (isCovered) distance?.localized() else stringResource(MR.strings.search_not_covered)
    return listOfNotNull(first, area).joinToString(" · ").takeIf { it.isNotEmpty() }
}

// Screen readers get the name, kind, distance and area as one sentence, and "not covered yet" for a place outside the area
@Composable
private fun accessibilityText(row: SuggestionRow): String {
    val suggestion = row.suggestion
    val kind = stringResource(suggestion.kind.word)
    val distance = row.distance.takeIf { suggestion.isCovered }?.localized()
    val notCovered = if (suggestion.isCovered) null else stringResource(MR.strings.search_not_covered_a11y)
    return listOfNotNull(suggestion.place.name, kind, distance, suggestion.place.area, notCovered).joinToString(", ")
}

@Composable
private fun recentAccessibilityText(row: RecentPlaceRow): String {
    val distance = row.distance.takeIf { row.isCovered }?.localized()
    val notCovered = if (row.isCovered) null else stringResource(MR.strings.search_not_covered_a11y)
    val name = row.place.name ?: stringResource(MR.strings.search_point_on_map)
    return listOfNotNull(name, stringResource(MR.strings.search_recent_a11y), distance, row.place.area, notCovered)
        .joinToString(", ")
}

private val PlaceKind.icon: ImageResource
    get() = when (this) {
        PlaceKind.Town -> MR.images.place_town
        PlaceKind.Street -> MR.images.place_street
        PlaceKind.Address -> MR.images.place_address
        PlaceKind.Nature -> MR.images.place_nature
        PlaceKind.PointOfInterest -> MR.images.place_poi
    }

private val SearchFailure.icon: ImageResource
    get() = when (this) {
        SearchFailure.Offline -> MR.images.offline
        SearchFailure.Failed -> MR.images.warning
    }

private val SearchFailure.title: StringResource
    get() = when (this) {
        SearchFailure.Offline -> MR.strings.search_offline_title
        SearchFailure.Failed -> MR.strings.search_failed_title
    }

private val SearchFailure.text: StringResource
    get() = when (this) {
        SearchFailure.Offline -> MR.strings.search_offline_text
        SearchFailure.Failed -> MR.strings.search_failed_text
    }

private val PlaceKind.word: StringResource
    get() = when (this) {
        PlaceKind.Town -> MR.strings.search_kind_town
        PlaceKind.Street -> MR.strings.search_kind_street
        PlaceKind.Address -> MR.strings.search_kind_address
        PlaceKind.Nature -> MR.strings.search_kind_nature
        PlaceKind.PointOfInterest -> MR.strings.search_kind_poi
    }
