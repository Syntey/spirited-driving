package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import kotlinx.coroutines.flow.StateFlow

interface AboutScreen {
    val viewState: StateFlow<AboutViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Privacy policy. */
        fun onPrivacyPolicyClick()

        /** Tapped Terms. */
        fun onTermsClick()

        /** Tapped Licences. */
        fun onLicencesClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onPrivacyPolicyClick() = Unit
                override fun onTermsClick() = Unit
                override fun onLicencesClick() = Unit
            }
        }
    }
}
