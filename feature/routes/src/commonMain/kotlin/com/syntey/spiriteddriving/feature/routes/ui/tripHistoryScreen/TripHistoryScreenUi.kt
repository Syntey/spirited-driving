package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AndroidTopBar
import com.syntey.spiriteddriving.core.ui.components.DragHandle
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.GroupPosition
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListHeader
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.MessageHost
import com.syntey.spiriteddriving.core.ui.components.MoreMenuButton
import com.syntey.spiriteddriving.core.ui.components.MoreMenuItem
import com.syntey.spiriteddriving.core.ui.components.PlaceholderRow
import com.syntey.spiriteddriving.core.ui.components.SmallTonalButton
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.components.showMessage
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.routes.model.ui.PastDriveRow
import com.syntey.spiriteddriving.feature.routes.model.ui.PastDriveRowEnd
import com.syntey.spiriteddriving.feature.routes.model.ui.RatingSheet
import com.syntey.spiriteddriving.feature.routes.model.ui.TripHistoryPreviewStates
import com.syntey.spiriteddriving.feature.routes.model.ui.TripHistorySection
import com.syntey.spiriteddriving.feature.routes.tools.Constants
import com.syntey.spiriteddriving.feature.routes.ui.components.RatingSheetContent
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TripHistoryScreenUi(screen: TripHistoryScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val changesOfflineText = stringResource(MR.strings.trip_history_offline_changes)
    val deleteFailedText = stringResource(MR.strings.trip_history_delete_failed)
    val clearFailedText = stringResource(MR.strings.trip_history_clear_failed)
    EventsEffect(screen.events) {
        onEvent<TripHistoryUiEvent.ChangesOffline> { hostState.showMessage(changesOfflineText) }
        onEvent<TripHistoryUiEvent.DeleteFailed> { hostState.showMessage(deleteFailedText) }
        onEvent<TripHistoryUiEvent.ClearFailed> { hostState.showMessage(clearFailedText) }
    }
    Box(modifier.fillMaxSize()) {
        Content(
            viewState = viewState,
            actions = screen.actions,
            title = screen.title.localized(),
            modifier = Modifier.fillMaxSize(),
        )
        MessageHost(hostState, Modifier.align(Alignment.BottomCenter))
    }
    // iOS shows the delete and clear confirmations as native alerts, from TripHistoryComponent.swift
    if (AppTheme.platform == UiPlatform.Android && viewState.isDeleteConfirmationShown) {
        DeleteDriveDialog(actions = screen.actions)
    }
    if (AppTheme.platform == UiPlatform.Android && viewState.isClearConfirmationShown) {
        ClearHistoryDialog(actions = screen.actions)
    }
    // iOS shows the rating sheet as a native sheet, from TripHistoryComponent.swift
    val ratingSheet = viewState.ratingSheet
    if (AppTheme.platform == UiPlatform.Android && ratingSheet != null) {
        RatingBottomSheet(sheet = ratingSheet, actions = screen.actions)
    }
}

@Composable
private fun Content(
    viewState: TripHistoryViewState,
    actions: TripHistoryScreen.Actions,
    title: String?,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        topBar = {
            AndroidTopBar(
                title = title,
                navigation = IconAction(
                    icon = painterResource(MR.images.back),
                    label = stringResource(MR.strings.generic_back),
                    onClick = actions::onClose,
                ),
                actions = {
                    if (viewState.canClear) ClearMenuButton(actions = actions)
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when (viewState.status) {
            TripHistoryStatus.Loading -> PlaceholderGroup(count = Constants.TripHistory.PLACEHOLDER_ROWS, modifier = contentModifier)

            TripHistoryStatus.Offline -> StateMessage(
                icon = painterResource(MR.images.offline),
                title = stringResource(MR.strings.trip_history_offline_title),
                message = stringResource(MR.strings.trip_history_offline_text),
                modifier = contentModifier,
                retry = retryAction(actions),
            )

            TripHistoryStatus.Failed -> StateMessage(
                icon = painterResource(MR.images.warning),
                title = stringResource(MR.strings.trip_history_failed_title),
                message = stringResource(MR.strings.trip_history_failed_text),
                modifier = contentModifier,
                retry = retryAction(actions),
            )

            TripHistoryStatus.Ready -> if (viewState.isEmpty) {
                StateMessage(
                    icon = painterResource(MR.images.recent),
                    title = stringResource(MR.strings.trip_history_empty_title),
                    message = stringResource(MR.strings.trip_history_empty_text),
                    modifier = contentModifier,
                )
            } else {
                DriveList(
                    sections = viewState.sections,
                    hasMore = viewState.hasMore,
                    isLoadingMore = viewState.isLoadingMore,
                    actions = actions,
                    modifier = contentModifier,
                )
            }
        }
    }
}

@Composable
private fun DriveList(
    sections: ImmutableList<TripHistorySection>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    actions: TripHistoryScreen.Actions,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - Constants.TripHistory.NEAR_END_ROWS
        }
    }
    // Keyed on isLoadingMore too, so a page that ends still near the end asks for the next one
    LaunchedEffect(nearEnd, hasMore, isLoadingMore) {
        if (nearEnd && hasMore) actions.onLoadMore()
    }
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = Grid.d4),
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            item(key = "header-$sectionIndex") {
                ListHeader(text = section.header.localized())
            }
            itemsIndexed(items = section.rows, key = { _, row -> row.id }) { index, row ->
                DriveItem(row = row, position = GroupPosition.of(index, section.rows.size), actions = actions)
            }
        }
        if (isLoadingMore) {
            item(key = "loading-more") {
                PlaceholderGroupItems(count = Constants.TripHistory.LOADING_MORE_ROWS)
            }
        }
    }
}

@Composable
private fun DriveItem(row: PastDriveRow, position: GroupPosition, actions: TripHistoryScreen.Actions) {
    val menuItems = listOf(
        MoreMenuItem(
            label = stringResource(MR.strings.trip_history_plan),
            onClick = { actions.onPlanNewRoutesClick(row.id) },
            icon = painterResource(MR.images.map),
        ),
        MoreMenuItem(
            label = stringResource(MR.strings.trip_history_delete),
            onClick = { actions.onDeleteClick(row.id) },
            icon = painterResource(MR.images.delete),
            isDestructive = true,
        ),
    )
    ListRow(
        headline = row.destination,
        supporting = row.details.localized(),
        onClick = { actions.onDriveClick(row.id) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DriveEnd(row = row, actions = actions)
                Spacer(Modifier.width(Grid.d1))
                MoreMenuButton(
                    items = menuItems,
                    contentDescription = stringResource(MR.strings.trip_history_more, row.destination),
                )
            }
        },
        position = position,
        contentDescription = row.description.localized(),
        swipeDelete = IconAction(
            icon = painterResource(MR.images.delete),
            label = stringResource(MR.strings.trip_history_delete),
            onClick = { actions.onDeleteClick(row.id) },
        ),
    )
}

@Composable
private fun DriveEnd(row: PastDriveRow, actions: TripHistoryScreen.Actions) {
    when (val end = row.end) {
        is PastDriveRowEnd.Rated -> Icon(
            painter = painterResource(
                if (end.thumb == Thumb.Up) MR.images.thumb_up_filled else MR.images.thumb_down_filled,
            ),
            contentDescription = null,
            modifier = Modifier.size(Grid.d5),
            tint = MaterialTheme.colorScheme.tertiary,
        )

        PastDriveRowEnd.Rate -> SmallTonalButton(
            text = stringResource(MR.strings.trip_history_rate),
            onClick = { actions.onRateClick(row.id) },
            contentDescription = stringResource(MR.strings.trip_history_rate_label, row.destination),
        )

        PastDriveRowEnd.None -> Unit
    }
}

@Composable
private fun ClearMenuButton(actions: TripHistoryScreen.Actions) {
    MoreMenuButton(
        items = listOf(
            MoreMenuItem(
                label = stringResource(MR.strings.trip_history_clear),
                onClick = actions::onClearClick,
                isDestructive = true,
            ),
        ),
        contentDescription = stringResource(MR.strings.generic_more),
    )
}

@Composable
private fun DeleteDriveDialog(actions: TripHistoryScreen.Actions) {
    AlertDialog(
        onDismissRequest = actions::onDeleteDismiss,
        icon = { Icon(painterResource(MR.images.delete), contentDescription = null) },
        title = { Text(stringResource(MR.strings.trip_history_delete_title)) },
        text = { Text(stringResource(MR.strings.trip_history_delete_text)) },
        dismissButton = {
            TextButton(onClick = actions::onDeleteDismiss) { Text(stringResource(MR.strings.generic_cancel)) }
        },
        confirmButton = {
            TextButton(onClick = actions::onDeleteConfirm) {
                Text(stringResource(MR.strings.trip_history_delete), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

@Composable
private fun ClearHistoryDialog(actions: TripHistoryScreen.Actions) {
    AlertDialog(
        onDismissRequest = actions::onClearDismiss,
        icon = { Icon(painterResource(MR.images.delete), contentDescription = null) },
        title = { Text(stringResource(MR.strings.trip_history_clear_title)) },
        text = { Text(stringResource(MR.strings.trip_history_clear_text)) },
        dismissButton = {
            TextButton(onClick = actions::onClearDismiss) { Text(stringResource(MR.strings.generic_cancel)) }
        },
        confirmButton = {
            TextButton(onClick = actions::onClearConfirm) {
                Text(stringResource(MR.strings.trip_history_clear_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingBottomSheet(sheet: RatingSheet, actions: TripHistoryScreen.Actions) {
    ModalBottomSheet(
        onDismissRequest = actions::onRatingSheetClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = Grid.d8, topEnd = Grid.d8),
        dragHandle = { DragHandle() },
    ) {
        RatingSheetContent(sheet = sheet, actions = actions)
    }
}

@Composable
private fun PlaceholderGroup(count: Int, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = Grid.d4)) {
        PlaceholderGroupItems(count = count, announce = true)
    }
}

// The placeholders of a group; the first one announces the loading, so a list is announced once
@Composable
private fun PlaceholderGroupItems(count: Int, announce: Boolean = false) {
    val loading = stringResource(MR.strings.trip_history_loading)
    repeat(count) { index ->
        PlaceholderRow(
            position = GroupPosition.of(index, count),
            announcement = loading.takeIf { announce && index == 0 },
        )
    }
}

@Composable
private fun retryAction(actions: TripHistoryScreen.Actions): IconAction = IconAction(
    icon = painterResource(MR.images.refresh),
    label = stringResource(MR.strings.generic_try_again),
    onClick = actions::onRetry,
)

@ScreenPreviews
@Composable
private fun TripHistoryScreenPreview() = Showcase {
    Content(
        viewState = TripHistoryPreviewStates.viewState(),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@ScreenPreviews
@Composable
private fun TripHistoryLoadingPreview() = Showcase {
    Content(
        viewState = TripHistoryPreviewStates.viewState(status = TripHistoryStatus.Loading, page = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@ScreenPreviews
@Composable
private fun TripHistoryLoadingMorePreview() = Showcase {
    Content(
        viewState = TripHistoryPreviewStates.viewState(isLoadingMore = true),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@ScreenPreviews
@Composable
private fun TripHistoryEmptyPreview() = Showcase {
    Content(
        viewState = TripHistoryPreviewStates.viewState(
            page = PastDrivePage(drives = emptyList(), nextCursor = null),
        ),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@ScreenPreviews
@Composable
private fun TripHistoryOfflinePreview() = Showcase {
    Content(
        viewState = TripHistoryPreviewStates.viewState(status = TripHistoryStatus.Offline, page = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@ScreenPreviews
@Composable
private fun TripHistoryFailedPreview() = Showcase {
    Content(
        viewState = TripHistoryPreviewStates.viewState(status = TripHistoryStatus.Failed, page = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@DarkLightPreviews
@Composable
private fun TripHistoryIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripHistoryPreviewStates.viewState(),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@DarkLightPreviews
@Composable
private fun TripHistoryLoadingIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripHistoryPreviewStates.viewState(status = TripHistoryStatus.Loading, page = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@DarkLightPreviews
@Composable
private fun TripHistoryLoadingMoreIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripHistoryPreviewStates.viewState(isLoadingMore = true),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@DarkLightPreviews
@Composable
private fun TripHistoryEmptyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripHistoryPreviewStates.viewState(
            page = PastDrivePage(drives = emptyList(), nextCursor = null),
        ),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@DarkLightPreviews
@Composable
private fun TripHistoryOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripHistoryPreviewStates.viewState(status = TripHistoryStatus.Offline, page = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@DarkLightPreviews
@Composable
private fun TripHistoryFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripHistoryPreviewStates.viewState(status = TripHistoryStatus.Failed, page = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.trip_history_title),
    )
}

@ScreenPreviews
@Composable
private fun TripHistoryClearDialogPreview() = Showcase(platform = UiPlatform.Android) {
    ClearHistoryDialog(actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun TripHistoryDeleteDialogPreview() = Showcase(platform = UiPlatform.Android) {
    DeleteDriveDialog(actions = noOpActions())
}
