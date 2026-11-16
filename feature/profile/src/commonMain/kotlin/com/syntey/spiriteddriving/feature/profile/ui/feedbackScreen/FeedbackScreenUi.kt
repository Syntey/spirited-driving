package com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.AndroidTopBar
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.NoteDefaultColors
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.StateMessage
import com.syntey.spiriteddriving.core.ui.components.TextArea
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.profile.model.ui.FeedbackPreviewStates
import com.syntey.spiriteddriving.feature.profile.model.ui.FeedbackStatus
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun FeedbackScreenUi(screen: FeedbackScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, title = screen.title.localized(), modifier = modifier)
}

@Composable
private fun Content(
    viewState: FeedbackViewState,
    actions: FeedbackScreen.Actions,
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
                    onClick = actions::onBack,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        if (viewState.status == FeedbackStatus.Sent) {
            StateMessage(
                icon = painterResource(MR.images.check),
                title = stringResource(MR.strings.feedback_sent_title),
                message = stringResource(MR.strings.feedback_sent_text),
                retry = IconAction(
                    icon = painterResource(MR.images.check),
                    label = stringResource(MR.strings.feedback_done),
                    onClick = actions::onDoneClick,
                ),
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            // The keyboard shrinks the column, so the field and Send scroll clear of it
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = AppTheme.dimensions.contentPadding,
                        top = Grid.d4,
                        end = AppTheme.dimensions.contentPadding,
                        bottom = AppTheme.dimensions.bottomPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(Grid.d3),
            ) {
                TextArea(
                    value = viewState.message,
                    onValueChange = actions::onMessageChange,
                    placeholder = stringResource(MR.strings.feedback_placeholder),
                    enabled = viewState.isEditable,
                )
                Text(
                    text = viewState.sentWith.localized(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (viewState.status) {
                    FeedbackStatus.Offline -> ErrorNote(text = MR.strings.feedback_offline, icon = MR.images.offline)
                    FeedbackStatus.Failed -> ErrorNote(text = MR.strings.feedback_failed, icon = MR.images.warning)
                    else -> Unit
                }
                AppButton(
                    text = stringResource(MR.strings.feedback_send),
                    onClick = actions::onSendClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewState.canSend,
                    isLoading = viewState.status == FeedbackStatus.Sending,
                )
            }
        }
    }
}

@Composable
private fun ErrorNote(text: StringResource, icon: ImageResource) {
    NoteCard(text = stringResource(text), icon = painterResource(icon), colors = NoteDefaultColors.error())
}

@ScreenPreviews
@Composable
private fun FeedbackScreenEmptyPreview() = Showcase {
    Content(
        viewState = FeedbackPreviewStates.viewState(),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun FeedbackScreenEmptyIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = FeedbackPreviewStates.viewState(),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@ScreenPreviews
@Composable
private fun FeedbackScreenTextPreview() = Showcase {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun FeedbackScreenTextIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@ScreenPreviews
@Composable
private fun FeedbackScreenSendingPreview() = Showcase {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE, status = FeedbackStatus.Sending),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun FeedbackScreenSendingIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE, status = FeedbackStatus.Sending),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@ScreenPreviews
@Composable
private fun FeedbackScreenOfflinePreview() = Showcase {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE, status = FeedbackStatus.Offline),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun FeedbackScreenOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE, status = FeedbackStatus.Offline),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@ScreenPreviews
@Composable
private fun FeedbackScreenFailedPreview() = Showcase {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE, status = FeedbackStatus.Failed),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun FeedbackScreenFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = FeedbackPreviewStates.viewState(message = FeedbackPreviewStates.SAMPLE_MESSAGE, status = FeedbackStatus.Failed),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@ScreenPreviews
@Composable
private fun FeedbackScreenSentPreview() = Showcase {
    Content(
        viewState = FeedbackPreviewStates.viewState(status = FeedbackStatus.Sent),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun FeedbackScreenSentIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = FeedbackPreviewStates.viewState(status = FeedbackStatus.Sent),
        actions = noOpActions(),
        title = stringResource(MR.strings.feedback_screen_title),
    )
}
