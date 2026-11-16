package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnResume
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.IsDriveRemindersOnUseCase
import com.syntey.spiriteddriving.feature.profile.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.profile.domain.ObserveVignetteCountriesUseCase
import com.syntey.spiriteddriving.feature.profile.domain.OpenDriveReminderSettingsUseCase
import com.syntey.spiriteddriving.feature.profile.domain.SetDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.profile.domain.SetVignetteUseCase
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class SettingsComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: SettingsScreenNavigation,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val setDistanceUnitUseCase: SetDistanceUnitUseCase,
    private val observeVignetteCountriesUseCase: ObserveVignetteCountriesUseCase,
    private val setVignetteUseCase: SetVignetteUseCase,
    private val isDriveRemindersOnUseCase: IsDriveRemindersOnUseCase,
    private val openDriveReminderSettingsUseCase: OpenDriveReminderSettingsUseCase,
) : ScreenComponent<SettingsViewState, Nothing, SettingsScreenNavigation>(
    componentContext = componentContext,
    defaultState = SettingsViewState(),
),
    SettingsScreen,
    SettingsScreenNavigation by navigation,
    SettingsScreen.Actions {

    private val logger = Logger.withTag("SettingsComponent")

    override val viewState: StateFlow<SettingsViewState> = componentState
    override val title: StringDesc = MR.strings.settings_screen_title.desc()
    override val actions: SettingsScreen.Actions = this

    init {
        // Read again on each return, as the user may have changed the notification in the system settings
        doOnResume(isOneTime = false) { readDriveReminders() }
        observeDistanceUnit()
        observeVignetteCountries()
    }

    override fun onBack() = pop()
    override fun onDriveRemindersClick() = openDriveReminderSettings()
    override fun onDistanceUnitSelect(unit: DistanceUnit) = saveDistanceUnit(unit)

    override fun onVignetteToggle(country: CountryCode) {
        val row = componentState.value.vignettes.firstOrNull { it.code == country } ?: return
        saveVignette(country, hasVignette = !row.hasVignette)
    }

    private fun observeDistanceUnit() = observeDistanceUnitUseCase.execute {
        onNext { unit -> componentState.update { it.copy(distanceUnit = unit) } }
        onError { error -> logger.e(error) { "Distance unit not observed" } }
    }

    private fun saveDistanceUnit(unit: DistanceUnit) = setDistanceUnitUseCase.execute(unit) {
        onError { error -> logger.e(error) { "Distance unit not kept" } }
    }

    private fun observeVignetteCountries() = observeVignetteCountriesUseCase.execute {
        onNext { countries -> componentState.update { it.copy(vignettes = countries.toImmutableList()) } }
        onError { error -> logger.e(error) { "Vignette countries not observed" } }
    }

    private fun readDriveReminders() = isDriveRemindersOnUseCase.execute {
        onSuccess { isOn -> componentState.update { it.copy(driveRemindersOn = isOn) } }
        onError { error -> logger.e(error) { "Drive reminders not read" } }
    }

    private fun openDriveReminderSettings() = openDriveReminderSettingsUseCase.execute {
        onError { error -> logger.e(error) { "Drive reminder settings not opened" } }
    }

    private fun saveVignette(country: CountryCode, hasVignette: Boolean) =
        setVignetteUseCase.execute(SetVignetteUseCase.Args(country = country, hasVignette = hasVignette)) {
            onError { error -> logger.e(error) { "Vignette not kept" } }
        }
}
