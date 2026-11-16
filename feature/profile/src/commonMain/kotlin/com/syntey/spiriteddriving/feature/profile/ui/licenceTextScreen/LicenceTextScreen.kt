package com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow

interface LicenceTextScreen {
    /** The library's name, which the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<LicenceTextViewState>
    val actions: Actions

    interface Actions {
        /** Tapped a licence that has no text here, but a page with it. */
        fun onLicenceLinkClick(url: String)

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onLicenceLinkClick(url: String) = Unit
                override fun onBack() = Unit
            }
        }
    }
}
