package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsScreen {
    val viewState: StateFlow<SettingsViewState>
    val actions: Actions

    /** One-time events for the screen, such as opening a web page. */
    val events: Flow<SettingsUiEvent>

    interface Actions {
        /** Tapped Privacy policy. */
        fun onPrivacyPolicyClick()

        /** Tapped Terms. */
        fun onTermsClick()

        /** Picked [unit] for distances. */
        fun onDistanceUnitSelect(unit: DistanceUnit)

        /** Tapped the row of [country]: flips whether the user has its motorway vignette. */
        fun onVignetteToggle(country: CountryCode)

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onPrivacyPolicyClick() = Unit
                override fun onTermsClick() = Unit
                override fun onDistanceUnitSelect(unit: DistanceUnit) = Unit
                override fun onVignetteToggle(country: CountryCode) = Unit
            }
        }
    }
}
