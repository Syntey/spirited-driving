package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import kotlinx.coroutines.flow.StateFlow

/**
 * The map and its sheet: one screen for a whole trip, from searching for a destination to the drive.
 */
interface TripPlanScreen {
    val viewState: StateFlow<TripPlanViewState>
    val actions: Actions

    interface Actions {
        /** Tapped the search field on the collapsed sheet. */
        fun onSearchClick()

        /** Tapped the locate button. */
        fun onLocateClick()

        /** Moved the map by hand, so it stops following the user. */
        fun onMapPanned()

        /** Tapped the profile button. */
        fun onProfileClick()

        /** Tapped Turn on in the "Precise location is off" note. */
        fun onPreciseLocationClick()

        /** Closed the "Precise location is off" note. */
        fun onPreciseLocationNoteClose()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onSearchClick() = Unit
                override fun onLocateClick() = Unit
                override fun onMapPanned() = Unit
                override fun onProfileClick() = Unit
                override fun onPreciseLocationClick() = Unit
                override fun onPreciseLocationNoteClose() = Unit
            }
        }
    }
}
