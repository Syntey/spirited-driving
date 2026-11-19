package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.GoogleSignInButton
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.NoteDefaultColors
import com.syntey.spiriteddriving.core.ui.components.SheetHeader
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import com.syntey.spiriteddriving.feature.profile.model.ui.SignInSheetPreviewStates
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.desc

/** The sign-in sheet's content. The host draws the sheet, so this has no background of its own. */
@Composable
fun SignInSheetScreenUi(screen: SignInSheetScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(
    viewState: SignInSheetViewState,
    actions: SignInSheetScreen.Actions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .padding(start = Grid.d4, top = Grid.d2, end = Grid.d4, bottom = Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d4),
    ) {
        SheetHeader(
            title = viewState.title.localized(),
            close = IconAction(
                icon = painterResource(MR.images.close),
                label = stringResource(MR.strings.generic_close),
                onClick = actions::onClose,
            ),
        )
        Text(
            text = stringResource(MR.strings.profile_sign_in_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        viewState.signInError?.let { error ->
            NoteCard(
                text = error.localized(),
                colors = NoteDefaultColors.error(),
                icon = painterResource(MR.images.warning),
            )
        }
        GoogleSignInButton(
            text = stringResource(MR.strings.sign_in_continue_google),
            logo = painterResource(MR.images.google_logo),
            onClick = actions::onGoogleClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = viewState.isSigningIn,
        )
    }
}

@ScreenPreviews
@Composable
private fun SignInSheetScreenPreview() = Showcase {
    Content(viewState = SignInSheetPreviewStates.viewState(), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun SignInSheetScreenSigningInPreview() = Showcase {
    Content(
        viewState = SignInSheetPreviewStates.viewState(reason = SignInReason.SavedRoutes, isSigningIn = true),
        actions = noOpActions(),
    )
}

@ScreenPreviews
@Composable
private fun SignInSheetScreenErrorPreview() = Showcase {
    Content(
        viewState = SignInSheetPreviewStates.viewState(signInError = MR.strings.sign_in_failed.desc()),
        actions = noOpActions(),
    )
}
