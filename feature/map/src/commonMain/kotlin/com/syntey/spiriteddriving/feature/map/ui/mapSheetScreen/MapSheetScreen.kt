package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The map and its sheet: one screen for a whole trip, from searching for a destination to the drive.
 */
interface MapSheetScreen {
    val viewState: StateFlow<MapSheetViewState>

    /** The kind of the sheet step on top, which decides the sheet's content. */
    val activeStep: StateFlow<ActiveSheetStep>
    val actions: Actions

    /** One-time events for the screen, such as an explanation after a tap. */
    val events: Flow<MapSheetUiEvent>

    /** Called by navigation when the sign-in sheet opened by Save closed. [isSignedIn] is whether the user signed in. */
    fun onSignInToSaveFinished(isSignedIn: Boolean)

    /** Called by navigation when a saved route was chosen in Saved routes: it shows on the map, ready to drive. */
    fun showSavedRoute(savedRouteId: SavedRouteId)

    /** Called by navigation when a drive was chosen in Trip history: its route shows on the map, ready to drive. */
    fun showPastDrive(driveId: String)

    /**
     * Called by navigation when Plan new routes was tapped in Trip history: plans to the drive's destination from where the user
     * is now, with the budget last used.
     */
    fun planNewRoutes(driveId: String)

    // Each tap and drag on the screen is a function here
    @Suppress("TooManyFunctions")
    interface Actions {
        /** Focused the search field. */
        fun onSearchFocus()

        /** Changed the text in the search field. */
        fun onSearchTextChange(text: String)

        /** Tapped the clear button in the search field. */
        fun onSearchClear()

        /** Closed the search or the start step: Cancel on iOS, the back arrow or Back on Android. */
        fun onSearchClose()

        /** Dragged the sheet to [height]. */
        fun onSheetHeightChange(height: SheetHeight)

        /** Tapped a suggestion. */
        fun onSuggestionClick(suggestion: PlaceSuggestion)

        /** Tapped a recent place. */
        fun onRecentPlaceClick(place: Place)

        /** Swiped a recent place away. */
        fun onRecentPlaceRemove(place: Place)

        /** Tapped My location in the start step. */
        fun onMyLocationClick()

        /** Tapped Try again after a failed search. */
        fun onSearchRetry()

        /** Tapped the locate button. */
        fun onLocateClick()

        /** Moved the map by hand, so it stops following the user. */
        fun onMapPanned()

        /** Touched and held the map at [point]: the start while the start step is open, else the destination. */
        fun onMapLongPress(point: GeoPoint)

        /** Tapped a route's line on the map. */
        fun onRouteClick(routeId: String)

        /** Tapped the profile button. */
        fun onProfileClick()

        /** Tapped Turn on in the "Precise location is off" note. */
        fun onPreciseLocationClick()

        /** Closed the "Precise location is off" note. */
        fun onPreciseLocationNoteClose()

        /** Tapped Drive. */
        fun onDriveClick()

        /** Tapped Open Apple Maps or Open Google Maps in the first-drive dialog. */
        fun onFirstDriveConfirm()

        /** Tapped Cancel in a drive dialog, or dismissed it. */
        fun onDriveDialogDismiss()

        /** Tapped Get Apple Maps or Open Play Store in the maps-app-missing dialog. */
        fun onMapsAppStoreClick()

        /** Tapped the reminder bubble. */
        fun onReminderClick()

        /** Dismissed the reminder bubble. */
        fun onReminderDismiss()

        /** Tapped Save, or Saved, in route planning. */
        fun onSaveClick()

        /** Tapped the Saved routes row in the search step. */
        fun onSavedRoutesClick()

        /** Tapped Save in the name dialog, with the name typed. */
        fun onRouteNameSave(name: String)

        /** Cancelled or dismissed the name dialog. */
        fun onRouteNameDismiss()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onSearchFocus() = Unit
                override fun onSearchTextChange(text: String) = Unit
                override fun onSearchClear() = Unit
                override fun onSearchClose() = Unit
                override fun onSheetHeightChange(height: SheetHeight) = Unit
                override fun onSuggestionClick(suggestion: PlaceSuggestion) = Unit
                override fun onRecentPlaceClick(place: Place) = Unit
                override fun onRecentPlaceRemove(place: Place) = Unit
                override fun onMyLocationClick() = Unit
                override fun onSearchRetry() = Unit
                override fun onLocateClick() = Unit
                override fun onMapPanned() = Unit
                override fun onMapLongPress(point: GeoPoint) = Unit
                override fun onRouteClick(routeId: String) = Unit
                override fun onProfileClick() = Unit
                override fun onPreciseLocationClick() = Unit
                override fun onPreciseLocationNoteClose() = Unit
                override fun onDriveClick() = Unit
                override fun onFirstDriveConfirm() = Unit
                override fun onDriveDialogDismiss() = Unit
                override fun onMapsAppStoreClick() = Unit
                override fun onReminderClick() = Unit
                override fun onReminderDismiss() = Unit
                override fun onSaveClick() = Unit
                override fun onRouteNameSave(name: String) = Unit
                override fun onRouteNameDismiss() = Unit
                override fun onSavedRoutesClick() = Unit
            }
        }
    }
}
