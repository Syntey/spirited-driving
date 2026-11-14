package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.profile.ui.components.WebLinkIcon
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ProfileScreenUi(screen: ProfileScreen, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    EventsEffect(screen.events) {
        onEvent<ProfileUiEvent.OpenWebPage> { uriHandler.openUri(it.url) }
    }
    Content(actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(actions: ProfileScreen.Actions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        ListGroup {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.profile_settings),
                    supporting = stringResource(MR.strings.profile_settings_supporting),
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
                    trailing = { WebLinkIcon() },
                    onClick = actions::onRateClick,
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.profile_send_feedback),
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
                    onClick = actions::onAboutClick,
                    chevron = painterResource(MR.images.chevron_right),
                    position = position,
                )
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
