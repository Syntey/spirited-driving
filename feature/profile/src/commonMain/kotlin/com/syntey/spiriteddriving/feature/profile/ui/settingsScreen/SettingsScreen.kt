package com.syntey.spiriteddriving.feature.profile.ui.settingsScreen

import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow

interface SettingsScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<SettingsViewState>
    val actions: Actions

    interface Actions {
        /** Picked [unit] for distances. */
        fun onDistanceUnitSelect(unit: DistanceUnit)

        /** Tapped the row of [country]: flips whether the user has its motorway vignette. */
        fun onVignetteToggle(country: CountryCode)

        /** Tapped Drive reminders: opens the phone's settings for the notification. */
        fun onDriveRemindersClick()

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onDistanceUnitSelect(unit: DistanceUnit) = Unit
                override fun onVignetteToggle(country: CountryCode) = Unit
                override fun onDriveRemindersClick() = Unit
                override fun onBack() = Unit
            }
        }
    }
}
