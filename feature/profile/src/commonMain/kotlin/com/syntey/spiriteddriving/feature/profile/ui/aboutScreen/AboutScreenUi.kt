package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.AndroidTopBar
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.profile.ui.components.WebLinkIcon
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.desc

@Composable
fun AboutScreenUi(screen: AboutScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, title = screen.title.localized(), modifier = modifier)
}

@Composable
private fun Content(
    viewState: AboutViewState,
    actions: AboutScreen.Actions,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(top = Grid.d4),
        ) {
            ListGroup {
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.about_privacy_policy),
                        trailing = { WebLinkIcon() },
                        onClick = actions::onPrivacyPolicyClick,
                        position = position,
                    )
                }
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.about_terms),
                        trailing = { WebLinkIcon() },
                        onClick = actions::onTermsClick,
                        position = position,
                    )
                }
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.about_licences),
                        chevron = painterResource(MR.images.chevron_right),
                        onClick = actions::onLicencesClick,
                        position = position,
                    )
                }
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.about_version),
                        value = viewState.version.localized(),
                        position = position,
                    )
                }
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun AboutScreenPreview() = Showcase {
    Content(
        viewState = AboutViewState(version = "1.0.0 (42)".desc()),
        actions = noOpActions(),
        title = stringResource(MR.strings.about_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun AboutScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = AboutViewState(version = "1.0.0 (42)".desc()),
        actions = noOpActions(),
        title = stringResource(MR.strings.about_screen_title),
    )
}
