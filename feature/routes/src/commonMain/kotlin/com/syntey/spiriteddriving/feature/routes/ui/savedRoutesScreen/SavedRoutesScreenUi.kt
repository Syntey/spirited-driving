package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.domain.savedroute.SAVED_ROUTE_NAME_MAX_LENGTH
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AndroidTopBar
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.CarSpinner
import com.syntey.spiriteddriving.core.ui.components.FunMeter
import com.syntey.spiriteddriving.core.ui.components.GroupPosition
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.MessageHost
import com.syntey.spiriteddriving.core.ui.components.MoreMenuButton
import com.syntey.spiriteddriving.core.ui.components.MoreMenuItem
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.components.TextFieldDialog
import com.syntey.spiriteddriving.core.ui.components.showMessage
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.routes.model.ui.RenameDialog
import com.syntey.spiriteddriving.feature.routes.model.ui.SavedRouteRow
import com.syntey.spiriteddriving.feature.routes.model.ui.SavedRoutesPreviewStates
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SavedRoutesScreenUi(screen: SavedRoutesScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val deletedText = stringResource(MR.strings.saved_routes_deleted)
    val undoText = stringResource(MR.strings.saved_routes_undo)
    val changesOfflineText = stringResource(MR.strings.saved_routes_offline_changes)
    val renameFailedText = stringResource(MR.strings.saved_routes_rename_failed)
    val deleteFailedText = stringResource(MR.strings.saved_routes_delete_failed)
    // Each message is shown in turn: a deleted route's delete waits for its message to end, or for its Undo
    EventsEffect(screen.events) {
        onEvent<SavedRoutesUiEvent.RouteDeleted> { event ->
            if (hostState.showMessage(deletedText, undoText)) {
                screen.actions.onDeleteUndo(event.savedRouteId)
            } else {
                screen.actions.onDeleteMessageGone(event.savedRouteId)
            }
        }
        onEvent<SavedRoutesUiEvent.ChangesOffline> { hostState.showMessage(changesOfflineText) }
        onEvent<SavedRoutesUiEvent.RenameFailed> { hostState.showMessage(renameFailedText) }
        onEvent<SavedRoutesUiEvent.DeleteFailed> { hostState.showMessage(deleteFailedText) }
    }
    Box(modifier) {
        Content(
            viewState = viewState,
            actions = screen.actions,
            title = screen.title.localized(),
            modifier = Modifier.fillMaxSize(),
        )
        MessageHost(hostState, Modifier.align(Alignment.BottomCenter))
    }
    // iOS shows the rename as a native alert, from SavedRoutesComponentModel.swift
    val renameDialog = viewState.renameDialog
    if (AppTheme.platform == UiPlatform.Android && renameDialog != null) {
        RenameTextDialog(dialog = renameDialog, actions = screen.actions)
    }
}

@Composable
private fun Content(
    viewState: SavedRoutesViewState,
    actions: SavedRoutesScreen.Actions,
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
            )
        },
        modifier = modifier,
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when (viewState.status) {
            SavedRoutesStatus.Loading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                CarSpinner()
            }

            SavedRoutesStatus.Offline -> StateMessage(
                icon = painterResource(MR.images.offline),
                title = stringResource(MR.strings.saved_routes_offline_title),
                message = stringResource(MR.strings.saved_routes_offline_text),
                modifier = contentModifier,
                retry = retryAction(actions),
            )

            SavedRoutesStatus.Failed -> StateMessage(
                icon = painterResource(MR.images.warning),
                title = stringResource(MR.strings.saved_routes_failed_title),
                message = stringResource(MR.strings.saved_routes_failed_text),
                modifier = contentModifier,
                retry = retryAction(actions),
            )

            SavedRoutesStatus.Ready -> if (viewState.isEmpty) {
                StateMessage(
                    icon = painterResource(MR.images.bookmark),
                    title = stringResource(MR.strings.saved_routes_empty_title),
                    message = stringResource(MR.strings.saved_routes_empty_text),
                    modifier = contentModifier,
                )
            } else {
                RouteList(routes = viewState.routes, actions = actions, modifier = contentModifier)
            }
        }
    }
}

@Composable
private fun RouteList(
    routes: ImmutableList<SavedRouteRow>,
    actions: SavedRoutesScreen.Actions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(vertical = Grid.d4)) {
        itemsIndexed(items = routes, key = { _, row -> row.id }) { index, row ->
            SavedRouteItem(row = row, position = GroupPosition.of(index, routes.size), actions = actions)
        }
    }
}

@Composable
private fun SavedRouteItem(row: SavedRouteRow, position: GroupPosition, actions: SavedRoutesScreen.Actions) {
    val menuItems = listOf(
        MoreMenuItem(
            label = stringResource(MR.strings.saved_routes_rename),
            onClick = { actions.onRenameClick(row.id) },
            icon = painterResource(MR.images.edit),
        ),
        MoreMenuItem(
            label = stringResource(MR.strings.saved_routes_delete),
            onClick = { actions.onDeleteClick(row.id) },
            icon = painterResource(MR.images.delete),
            isDestructive = true,
        ),
    )
    ListRow(
        headline = row.name,
        supporting = row.details.localized(),
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FunMeter(
                    level = row.funLevel,
                    label = stringResource(MR.strings.fun_meter),
                    contentDescription = stringResource(MR.strings.fun_meter_description, row.funLevel),
                )
                Spacer(Modifier.width(Grid.d1))
                MoreMenuButton(
                    items = menuItems,
                    contentDescription = stringResource(MR.strings.saved_routes_more, row.name),
                )
            }
        },
        onClick = { actions.onRouteClick(row.id) },
        swipeDelete = IconAction(
            icon = painterResource(MR.images.delete),
            label = stringResource(MR.strings.saved_routes_delete),
            onClick = { actions.onDeleteClick(row.id) },
        ),
        position = position,
    )
}

@Composable
private fun RenameTextDialog(dialog: RenameDialog, actions: SavedRoutesScreen.Actions) {
    val takenError = stringResource(MR.strings.saved_routes_name_taken)
    // Keyed by the dialog, so a new one starts with its own name rather than the text of the last
    key(dialog) {
        TextFieldDialog(
            title = stringResource(MR.strings.saved_routes_rename_title),
            message = null,
            initialText = dialog.currentName,
            confirmLabel = stringResource(MR.strings.planning_save),
            dismissLabel = stringResource(MR.strings.generic_cancel),
            error = { text -> takenError.takeIf { dialog.isTaken(text) } },
            canConfirm = dialog::canSave,
            maxLength = SAVED_ROUTE_NAME_MAX_LENGTH,
            onConfirm = actions::onRenameSave,
            onDismiss = actions::onRenameDismiss,
        )
    }
}

@Composable
private fun retryAction(actions: SavedRoutesScreen.Actions): IconAction = IconAction(
    icon = painterResource(MR.images.refresh),
    label = stringResource(MR.strings.generic_try_again),
    onClick = actions::onRetry,
)

@ScreenPreviews
@Composable
private fun SavedRoutesScreenPreview() = Showcase {
    Content(
        viewState = SavedRoutesPreviewStates.viewState(),
        actions = noOpActions(),
        title = stringResource(MR.strings.saved_routes_title),
    )
}

@ScreenPreviews
@Composable
private fun SavedRoutesLoadingPreview() = Showcase {
    Content(
        viewState = SavedRoutesPreviewStates.viewState(status = SavedRoutesStatus.Loading, savedRoutes = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.saved_routes_title),
    )
}

@ScreenPreviews
@Composable
private fun SavedRoutesEmptyPreview() = Showcase {
    Content(
        viewState = SavedRoutesPreviewStates.viewState(savedRoutes = emptyList()),
        actions = noOpActions(),
        title = stringResource(MR.strings.saved_routes_title),
    )
}

@ScreenPreviews
@Composable
private fun SavedRoutesOfflinePreview() = Showcase {
    Content(
        viewState = SavedRoutesPreviewStates.viewState(status = SavedRoutesStatus.Offline, savedRoutes = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.saved_routes_title),
    )
}

@ScreenPreviews
@Composable
private fun SavedRoutesFailedPreview() = Showcase {
    Content(
        viewState = SavedRoutesPreviewStates.viewState(status = SavedRoutesStatus.Failed, savedRoutes = null),
        actions = noOpActions(),
        title = stringResource(MR.strings.saved_routes_title),
    )
}

@DarkLightPreviews
@Composable
private fun SavedRoutesScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = SavedRoutesPreviewStates.viewState(),
        actions = noOpActions(),
        title = stringResource(MR.strings.saved_routes_title),
    )
}
