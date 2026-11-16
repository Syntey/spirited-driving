package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.AppScaffoldDefaultColors
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.ListRowIcon
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.profile.ui.components.ProfileHeader
import com.syntey.spiriteddriving.feature.profile.ui.components.WebLinkIcon
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ProfileScreenUi(screen: ProfileScreen, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    EventsEffect(screen.events) {
        // Rate opens the store app, not a web page, so it stays with the phone's handler and not the in-app browser
        onEvent<ProfileUiEvent.OpenWebPage> { uriHandler.openUri(it.url) }
    }
    Content(actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(
    actions: ProfileScreen.Actions,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = null,
        back = IconAction(
            icon = painterResource(MR.images.back),
            label = stringResource(MR.strings.generic_back),
            onClick = actions::onBackClick,
        ),
        modifier = modifier,
        // Profile draws behind the transparent top bar and keeps its own content clear of the insets
        colors = AppScaffoldDefaultColors.overArt(),
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            ProfileHeader()
            // The first card overlaps the bottom of the picture, so the road runs on behind it
            Column(
                modifier = Modifier
                    .offset(y = -Grid.d6)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    .padding(bottom = Grid.d4),
                verticalArrangement = Arrangement.spacedBy(Grid.d6),
            ) {
                ListGroup {
                    item { position ->
                        ListRow(
                            headline = stringResource(MR.strings.profile_settings),
                            supporting = stringResource(MR.strings.profile_settings_supporting),
                            leading = { ListRowIcon(painterResource(MR.images.settings)) },
                            chevron = painterResource(MR.images.chevron_right),
                            onClick = actions::onSettingsClick,
                            position = position,
                        )
                    }
                }
                ListGroup(header = stringResource(MR.strings.profile_feedback)) {
                    item { position ->
                        ListRow(
                            headline = stringResource(MR.strings.profile_rate),
                            leading = { ListRowIcon(painterResource(MR.images.rate)) },
                            trailing = { WebLinkIcon() },
                            onClick = actions::onRateClick,
                            position = position,
                        )
                    }
                    item { position ->
                        ListRow(
                            headline = stringResource(MR.strings.profile_send_feedback),
                            leading = { ListRowIcon(painterResource(MR.images.feedback)) },
                            onClick = actions::onSendFeedbackClick,
                            chevron = painterResource(MR.images.chevron_right),
                            position = position,
                        )
                    }
                }
                ListGroup {
                    item { position ->
                        ListRow(
                            headline = stringResource(MR.strings.profile_about),
                            leading = { ListRowIcon(painterResource(MR.images.about)) },
                            onClick = actions::onAboutClick,
                            chevron = painterResource(MR.images.chevron_right),
                            position = position,
                        )
                    }
                }
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun ProfileScreenPreview() = Showcase {
    Content(actions = noOpActions())
}

@DarkLightPreviews
@Composable
private fun ProfileScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(actions = noOpActions())
}
