package com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.profile.model.ui.LicenceTextPreviewStates
import com.syntey.spiriteddriving.feature.profile.ui.components.WebLinkIcon
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreen.Actions.Companion.noOpActions

@Composable
fun LicenceTextScreenUi(screen: LicenceTextScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(viewState: LicenceTextViewState, actions: LicenceTextScreen.Actions, modifier: Modifier = Modifier) {
    val showNames = viewState.licences.size > 1
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = Grid.d4, top = Grid.d4, end = Grid.d4, bottom = Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        viewState.licences.forEach { licence ->
            val text = licence.text
            val url = licence.url
            if (text != null) {
                Column(verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
                    if (showNames) {
                        Text(
                            text = licence.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    // Selectable so the text can be copied, which a plain Text doesn't allow
                    SelectionContainer {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            } else if (url != null) {
                ListGroup {
                    item { position ->
                        ListRow(
                            headline = licence.name,
                            trailing = { WebLinkIcon() },
                            onClick = { actions.onLicenceLinkClick(url) },
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
private fun LicenceTextScreenPreview() = Showcase {
    Content(
        viewState = LicenceTextPreviewStates.viewState(licences = LicenceTextPreviewStates.oneLicence),
        actions = noOpActions(),
    )
}

@ScreenPreviews
@Composable
private fun LicenceTextScreenTwoLicencesPreview() = Showcase {
    Content(
        viewState = LicenceTextPreviewStates.viewState(licences = LicenceTextPreviewStates.twoLicences),
        actions = noOpActions(),
    )
}

@DarkLightPreviews
@Composable
private fun LicenceTextScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = LicenceTextPreviewStates.viewState(licences = LicenceTextPreviewStates.twoLicences),
        actions = noOpActions(),
    )
}
