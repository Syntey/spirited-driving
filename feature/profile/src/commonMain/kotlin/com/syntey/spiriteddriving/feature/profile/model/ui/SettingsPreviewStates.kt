package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.profile.model.VignetteCountry
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsViewState
import dev.icerock.moko.resources.desc.desc
import kotlinx.collections.immutable.persistentListOf

internal object SettingsPreviewStates {
    fun viewState(distanceUnit: DistanceUnit = DistanceUnit.Kilometres) = SettingsViewState(
        version = "1.0.0 (42)".desc(),
        distanceUnit = distanceUnit,
        vignettes = persistentListOf(
            VignetteCountry(code = CountryCode.Czechia, name = "Czechia", hasVignette = true),
            VignetteCountry(code = CountryCode("SK"), name = "Slovakia", hasVignette = false),
            VignetteCountry(code = CountryCode("AT"), name = "Austria", hasVignette = false),
        ),
    )
}
