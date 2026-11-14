package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.profile.model.VignetteCountry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SettingsViewState(
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val vignettes: ImmutableList<VignetteCountry> = persistentListOf(),
    val driveRemindersOn: Boolean = false,
)
