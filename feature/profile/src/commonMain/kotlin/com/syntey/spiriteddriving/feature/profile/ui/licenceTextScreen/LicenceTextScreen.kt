package com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen

import kotlinx.coroutines.flow.StateFlow

interface LicenceTextScreen {
    /** The library's name, which the navigation hosts show as the title. */
    val title: String
    val viewState: StateFlow<LicenceTextViewState>
    val actions: Actions

    interface Actions {
        /** Tapped a licence that has no text here, but a page with it. */
        fun onLicenceLinkClick(url: String)

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onLicenceLinkClick(url: String) = Unit
            }
        }
    }
}
