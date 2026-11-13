package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.SegmentOption
import com.syntey.spiriteddriving.core.ui.components.SegmentedSwitch
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.ToggleSwitch
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.core.ui.tools.EventsEffect
import com.syntey.spiriteddriving.core.ui.tools.onEvent
import com.syntey.spiriteddriving.feature.profile.model.ui.SettingsPreviewStates
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

// The order of the Units switch's segments: kilometres, then miles
private val distanceUnits = listOf(DistanceUnit.Kilometres, DistanceUnit.Miles)

@Composable
fun SettingsScreenUi(screen: SettingsScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    EventsEffect(screen.events) {
        onEvent<SettingsUiEvent.OpenWebPage> { uriHandler.openUri(it.url) }
    }
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(viewState: SettingsViewState, actions: SettingsScreen.Actions, modifier: Modifier = Modifier) {
    // Each platform has its own web-link glyph: an arrow on iOS, the open-in-new icon on Android.
    val webLinkIcon = painterResource(
        if (AppTheme.platform == UiPlatform.Ios) MR.images.arrow_up_right else MR.images.open_in_new,
    )
    val webLink: @Composable () -> Unit = {
        Icon(
            painter = webLinkIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Grid.d5),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        ListGroup(header = stringResource(MR.strings.settings_units)) {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_distance),
                    trailing = {
                        SegmentedSwitch(
                            options = listOf(
                                SegmentOption(
                                    label = stringResource(MR.strings.planning_budget_kilometres),
                                    accessibilityLabel = stringResource(MR.strings.planning_budget_kilometres_description),
                                ),
                                SegmentOption(
                                    label = stringResource(MR.strings.planning_budget_miles),
                                    accessibilityLabel = stringResource(MR.strings.planning_budget_miles_description),
                                ),
                            ),
                            selectedIndex = distanceUnits.indexOf(viewState.distanceUnit),
                            onSelect = { actions.onDistanceUnitSelect(distanceUnits[it]) },
                        )
                    },
                    position = position,
                )
            }
        }
        ListGroup(
            header = stringResource(MR.strings.settings_vignettes),
            footer = stringResource(MR.strings.settings_vignettes_footer),
        ) {
            viewState.vignettes.forEach { country ->
                item { position ->
                    ListRow(
                        headline = country.name,
                        trailing = { ToggleSwitch(checked = country.hasVignette, onCheckedChange = null) },
                        onClick = { actions.onVignetteToggle(country.code) },
                        position = position,
                    )
                }
            }
        }
        ListGroup(
            header = stringResource(MR.strings.settings_notifications),
            footer = stringResource(MR.strings.settings_notifications_footer),
        ) {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_drive_reminders),
                    value = stringResource(if (viewState.driveRemindersOn) MR.strings.settings_on else MR.strings.settings_off),
                    onClick = actions::onDriveRemindersClick,
                    chevron = painterResource(MR.images.chevron_right),
                    position = position,
                )
            }
        }
        ListGroup(header = stringResource(MR.strings.settings_feedback)) {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_rate),
                    trailing = webLink,
                    onClick = actions::onRateClick,
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_send_feedback),
                    onClick = actions::onSendFeedbackClick,
                    chevron = painterResource(MR.images.chevron_right),
                    position = position,
                )
            }
        }
        ListGroup(header = stringResource(MR.strings.settings_about)) {
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_privacy_policy),
                    trailing = webLink,
                    onClick = actions::onPrivacyPolicyClick,
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_terms),
                    trailing = webLink,
                    onClick = actions::onTermsClick,
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    headline = stringResource(MR.strings.settings_version),
                    value = viewState.version.localized(),
                    position = position,
                )
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun SettingsScreenPreview() = Showcase {
    Content(viewState = SettingsPreviewStates.viewState(driveRemindersOn = true), actions = noOpActions())
}

@DarkLightPreviews
@Composable
private fun SettingsScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = SettingsPreviewStates.viewState(distanceUnit = DistanceUnit.Miles, driveRemindersOn = false),
        actions = noOpActions(),
    )
}
