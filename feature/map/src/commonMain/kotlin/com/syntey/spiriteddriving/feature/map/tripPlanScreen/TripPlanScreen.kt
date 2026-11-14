package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The map and its sheet: one screen for a whole trip, from searching for a destination to the drive.
 */
interface TripPlanScreen {
    val viewState: StateFlow<TripPlanViewState>
    val actions: Actions

    /** One-time events for the screen, such as an explanation after a tap. */
    val events: Flow<TripPlanUiEvent>

    interface Actions {
        /** Focused the search field. */
        fun onSearchFocus()

        /** Changed the text in the search field. */
        fun onSearchTextChange(text: String)

        /** Tapped the clear button in the search field. */
        fun onSearchClear()

        /** Closed the search: X on iOS, the back arrow or Back on Android. */
        fun onSearchClose()

        /** Dragged the sheet to [height]. */
        fun onSheetHeightChange(height: SheetHeight)

        /** Tapped a suggestion. */
        fun onSuggestionClick(suggestion: PlaceSuggestion)

        /** Tapped Try again after a failed search. */
        fun onSearchRetry()

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
                override fun onSearchFocus() = Unit
                override fun onSearchTextChange(text: String) = Unit
                override fun onSearchClear() = Unit
                override fun onSearchClose() = Unit
                override fun onSheetHeightChange(height: SheetHeight) = Unit
                override fun onSuggestionClick(suggestion: PlaceSuggestion) = Unit
                override fun onSearchRetry() = Unit
                override fun onLocateClick() = Unit
                override fun onMapPanned() = Unit
                override fun onProfileClick() = Unit
                override fun onPreciseLocationClick() = Unit
                override fun onPreciseLocationNoteClose() = Unit
            }
        }
    }
}
