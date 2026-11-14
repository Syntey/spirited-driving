package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AboutScreen {
    val viewState: StateFlow<AboutViewState>
    val actions: Actions

    /** One-time events for the screen, such as opening a web page. */
    val events: Flow<AboutUiEvent>

    interface Actions {
        /** Tapped Privacy policy. */
        fun onPrivacyPolicyClick()

        /** Tapped Terms. */
        fun onTermsClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onPrivacyPolicyClick() = Unit
                override fun onTermsClick() = Unit
            }
        }
    }
}
