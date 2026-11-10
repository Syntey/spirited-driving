package com.syntey.spiriteddriving.feature.sample.loginScreen

import kotlinx.coroutines.flow.StateFlow

interface LoginScreen {
    val viewState: StateFlow<LoginViewState>
    val actions: Actions

    interface Actions {
        fun onLoginClick()

        companion object {
            fun noOpActions() = object : Actions {
                override fun onLoginClick() = Unit
            }
        }
    }
}
