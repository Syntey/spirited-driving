package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.SegmentOption
import com.syntey.spiriteddriving.core.ui.components.SegmentedSwitch
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.ToggleSwitch
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
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
    Content(viewState = viewState, actions = screen.actions, title = screen.title.localized(), modifier = modifier)
}

@Composable
private fun Content(
    viewState: SettingsViewState,
    actions: SettingsScreen.Actions,
    title: String?,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = title,
        back = IconAction(
            icon = painterResource(MR.images.back),
            label = stringResource(MR.strings.generic_back),
            onClick = actions::onBackClick,
        ),
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        trailing = { ToggleSwitch(checked = viewState.driveRemindersOn, onCheckedChange = null) },
                        onClick = actions::onDriveRemindersClick,
                        position = position,
                    )
                }
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun SettingsScreenPreview() = Showcase {
    Content(
        viewState = SettingsPreviewStates.viewState(driveRemindersOn = true),
        actions = noOpActions(),
        title = stringResource(MR.strings.settings_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun SettingsScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = SettingsPreviewStates.viewState(distanceUnit = DistanceUnit.Miles, driveRemindersOn = false),
        actions = noOpActions(),
        title = stringResource(MR.strings.settings_screen_title),
    )
}
