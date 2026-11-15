package com.syntey.spiriteddriving.feature.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.GoogleSignInButton
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.NoteDefaultColors
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.StringDesc

/** Profile's sign-in card while signed out: what the account keeps, the error if the last try failed, and the button. */
@Composable
internal fun SignInCard(
    signInError: StringDesc?,
    isSigningIn: Boolean,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.platformStyle.list.groupInset)
            .background(AppTheme.customColors.listGroup, AppTheme.platformStyle.cardShape)
            .padding(Grid.d4),
    ) {
        Text(
            text = stringResource(MR.strings.profile_sign_in_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Grid.d1))
        Text(
            text = stringResource(MR.strings.profile_sign_in_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Grid.d4))
        if (signInError != null) {
            NoteCard(
                text = signInError.localized(),
                colors = NoteDefaultColors.error(),
                icon = painterResource(MR.images.warning),
            )
            Spacer(Modifier.height(Grid.d3))
        }
        GoogleSignInButton(
            text = stringResource(MR.strings.sign_in_continue_google),
            logo = painterResource(MR.images.google_logo),
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = isSigningIn,
        )
    }
}
