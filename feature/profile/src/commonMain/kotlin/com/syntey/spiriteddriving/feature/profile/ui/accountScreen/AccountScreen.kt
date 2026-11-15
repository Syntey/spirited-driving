package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import kotlinx.coroutines.flow.StateFlow

interface AccountScreen {
    val viewState: StateFlow<AccountViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Sign out. */
        fun onSignOutClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onSignOutClick() = Unit
            }
        }
    }
}
