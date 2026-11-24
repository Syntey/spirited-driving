package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute

import kotlinx.coroutines.flow.StateFlow

/** The one-route step of the map sheet, a saved route or a past drive: its own state and taps, which the sheet's UI uses directly. */
interface OneRouteScreen {
    val viewState: StateFlow<OneRouteViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Try again after the route didn't load. */
        fun onRetryClick()

        /** Tapped the close button in the header. */
        fun onCloseClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onRetryClick() = Unit
                override fun onCloseClick() = Unit
            }
        }
    }
}
