package com.syntey.spiriteddriving.feature.profile.ui.licencesScreen

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow

interface LicencesScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<LicencesViewState>
    val actions: Actions

    interface Actions {
        /** Tapped the OpenStreetMap attribution. */
        fun onOpenStreetMapClick()

        /** Tapped a library row, with its [id] and the [name] its screen shows as the title. */
        fun onLibraryClick(id: String, name: String)

        /** Tapped the Phosphor row, with its localized [name] for the licence screen's title. */
        fun onIconsClick(name: String)

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onOpenStreetMapClick() = Unit
                override fun onLibraryClick(id: String, name: String) = Unit
                override fun onIconsClick(name: String) = Unit
                override fun onBack() = Unit
            }
        }
    }
}
