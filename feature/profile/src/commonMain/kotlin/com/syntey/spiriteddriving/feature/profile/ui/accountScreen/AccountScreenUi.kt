package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.ListRowDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ListRowIcon
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.profile.model.ui.AccountPreviewStates
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun AccountScreenUi(screen: AccountScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
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
        ListGroup {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.account_sign_out),
                    leading = { ListRowIcon(painterResource(MR.images.sign_out)) },
                    colors = ListRowDefaultColors.action(),
                    onClick = actions::onSignOutClick,
                    position = position,
                )
            }
        }
    }
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
