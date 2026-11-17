package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
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

        /** Tapped "From" in route planning's header. */
        fun onStartClick()

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

        /** Tapped a route in the list or its line on the map. */
        fun onRouteClick(routeId: String)

        /** Tapped a budget chip. */
        fun onBudgetClick(budget: Budget)

        /** Switched the budget's unit. */
        fun onBudgetKindChange(kind: BudgetKind)

        /** Closed route planning: Cancel, or Back on Android. */
        fun onRoutePlanningClose()

        /** Tapped Try again after planning failed. */
        fun onPlanningRetry()

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

        /** Tapped Good or Not good in the rating step. */
        fun onRatingThumbClick(thumb: Thumb)

        /** Tapped a reason chip in the rating step. */
        fun onRatingReasonClick(reason: RatingReason)

        /** Tapped Done in the rating step. */
        fun onRatingDone()

        /** Closed the rating step: its close button, or Back on Android. */
        fun onRatingClose()

        /** Tapped I didn't drive it. */
        fun onNotDrivenClick()

        /** Tapped the reminder bubble. */
        fun onReminderClick()

        /** Dismissed the reminder bubble. */
        fun onReminderDismiss()

        /** Tapped Turn on in the reminder offer. */
        fun onReminderOfferClick()

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
                override fun onStartClick() = Unit
                override fun onMyLocationClick() = Unit
                override fun onSearchRetry() = Unit
                override fun onLocateClick() = Unit
                override fun onMapPanned() = Unit
                override fun onMapLongPress(point: GeoPoint) = Unit
                override fun onRouteClick(routeId: String) = Unit
                override fun onBudgetClick(budget: Budget) = Unit
                override fun onBudgetKindChange(kind: BudgetKind) = Unit
                override fun onRoutePlanningClose() = Unit
                override fun onPlanningRetry() = Unit
                override fun onProfileClick() = Unit
                override fun onPreciseLocationClick() = Unit
                override fun onPreciseLocationNoteClose() = Unit
                override fun onDriveClick() = Unit
                override fun onFirstDriveConfirm() = Unit
                override fun onDriveDialogDismiss() = Unit
                override fun onMapsAppStoreClick() = Unit
                override fun onRatingThumbClick(thumb: Thumb) = Unit
                override fun onRatingReasonClick(reason: RatingReason) = Unit
                override fun onRatingDone() = Unit
                override fun onRatingClose() = Unit
                override fun onNotDrivenClick() = Unit
                override fun onReminderClick() = Unit
                override fun onReminderDismiss() = Unit
                override fun onReminderOfferClick() = Unit
            }
        }
    }
}
