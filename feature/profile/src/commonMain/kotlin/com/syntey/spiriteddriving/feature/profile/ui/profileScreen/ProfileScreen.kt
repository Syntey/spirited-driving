package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import kotlinx.coroutines.flow.StateFlow

interface ProfileScreen {
    val viewState: StateFlow<ProfileViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Settings. */
        fun onSettingsClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onSettingsClick() = Unit
            }
        }
    }
}
