package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow

interface AboutScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<AboutViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Privacy policy. */
        fun onPrivacyPolicyClick()

        /** Tapped Terms. */
        fun onTermsClick()

        /** Tapped Licences. */
        fun onLicencesClick()

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onPrivacyPolicyClick() = Unit
                override fun onTermsClick() = Unit
                override fun onLicencesClick() = Unit
                override fun onBack() = Unit
            }
        }
    }
}
