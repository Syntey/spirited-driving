package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.CarSpinner
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.ListRowDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ListRowIcon
import com.syntey.spiriteddriving.core.ui.components.MessageHost
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.showMessage
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.profile.model.ui.AccountPreviewStates
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

@Composable
fun AccountScreenUi(screen: AccountScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deleteFailedMessage = stringResource(MR.strings.account_delete_failed)
    EventsEffect(screen.events) {
        onEvent<AccountUiEvent.DeleteFailed> { scope.launch { hostState.showMessage(deleteFailedMessage) } }
    }
    Box(modifier) {
        Content(viewState = viewState, actions = screen.actions, modifier = Modifier.fillMaxSize())
        MessageHost(hostState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Content(viewState: AccountViewState, actions: AccountScreen.Actions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        ListGroup(footer = viewState.footer.localized()) {
            viewState.name?.let { name ->
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.account_name),
                        value = name,
                        position = position,
                    )
                }
            }
            // The address can be long, so it sits under the label, where it wraps, rather than at the row's end
            viewState.email?.let { email ->
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.account_email),
                        supporting = email,
                        position = position,
                    )
                }
            }
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.account_signed_in_with),
                    value = viewState.providerName.localized(),
                    leading = if (viewState.provider == SignInProvider.Google) ({ GoogleLogo() }) else null,
                    position = position,
                )
            }
        }
        // The screen takes no input while the account is deleting, so neither row reacts
        ListGroup(footer = stringResource(MR.strings.account_delete_footer)) {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.account_sign_out),
                    leading = { ListRowIcon(painterResource(MR.images.sign_out)) },
                    colors = ListRowDefaultColors.action(),
                    onClick = if (viewState.isDeleting) null else actions::onSignOutClick,
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.account_delete),
                    leading = {
                        if (viewState.isDeleting) {
                            CarSpinner(Modifier.size(AppTheme.platformStyle.list.iconSize), color = MaterialTheme.colorScheme.error)
                        } else {
                            ListRowIcon(painterResource(MR.images.delete))
                        }
                    },
                    colors = ListRowDefaultColors.error(),
                    onClick = if (viewState.isDeleting) null else actions::onDeleteAccountClick,
                    position = position,
                )
            }
        }
    }
    // iOS shows the same confirmation as a native alert, from AccountComponent.swift
    if (AppTheme.platform == UiPlatform.Android && viewState.isDeleteDialogShown) {
        DeleteAccountDialog(actions = actions)
    }
}

@Composable
private fun DeleteAccountDialog(actions: AccountScreen.Actions) {
    AlertDialog(
        onDismissRequest = actions::onDeleteDialogDismiss,
        icon = { Icon(painterResource(MR.images.delete), contentDescription = null) },
        title = { Text(stringResource(MR.strings.account_delete_dialog_title)) },
        text = { Text(stringResource(MR.strings.account_delete_dialog_message)) },
        dismissButton = {
            TextButton(onClick = actions::onDeleteDialogDismiss) { Text(stringResource(MR.strings.generic_cancel)) }
        },
        confirmButton = {
            TextButton(onClick = actions::onDeleteConfirm) {
                Text(stringResource(MR.strings.generic_delete), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

// Multi-coloured, so it's drawn as it is rather than tinted
@Composable
private fun GoogleLogo() {
    Image(
        painter = painterResource(MR.images.google_logo),
        contentDescription = null,
        modifier = Modifier.size(AppTheme.platformStyle.list.iconSize),
    )
}

@ScreenPreviews
@Composable
private fun AccountScreenPreview() = Showcase {
    Content(viewState = AccountPreviewStates.googleViewState(), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun AccountScreenWithoutNamePreview() = Showcase {
    Content(viewState = AccountPreviewStates.appleViewState(), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun AccountScreenDeletingPreview() = Showcase {
    Content(viewState = AccountPreviewStates.deletingViewState(), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun AccountScreenDeleteDialogPreview() = Showcase(platform = UiPlatform.Android) {
    Content(viewState = AccountPreviewStates.deleteDialogViewState(), actions = noOpActions())
}

@DarkLightPreviews
@Composable
private fun AccountScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = AccountPreviewStates.googleViewState(), actions = noOpActions())
}

@DarkLightPreviews
@Composable
private fun AccountScreenIosWithoutNamePreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = AccountPreviewStates.appleViewState(), actions = noOpActions())
}

@DarkLightPreviews
@Composable
private fun AccountScreenIosDeletingPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = AccountPreviewStates.deletingViewState(), actions = noOpActions())
}
