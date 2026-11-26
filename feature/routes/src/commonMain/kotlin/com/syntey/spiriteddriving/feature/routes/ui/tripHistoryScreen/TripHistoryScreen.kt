package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TripHistoryScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<TripHistoryViewState>
    val actions: Actions

    /** One-time events for the screen, such as a message after a change. */
    val events: Flow<TripHistoryUiEvent>

    interface Actions {
        /** Tapped the Rate button of a drive. */
        fun onRateClick(id: String)

        /** Tapped Good or Not good in the rating sheet. */
        fun onRatingThumbClick(thumb: Thumb)

        /** Tapped a reason chip in the rating sheet. */
        fun onRatingReasonClick(reason: RatingReason)

        /** Closed the rating sheet with Done, its close button, a swipe down or Back. */
        fun onRatingSheetClose()

        /** Tapped a row: the drive shows on the map. */
        fun onDriveClick(id: String)

        /** Chose Plan new routes in a row's menu. */
        fun onPlanNewRoutesClick(id: String)

        /** Chose Delete in a row's menu, or swiped the row away: asks first. */
        fun onDeleteClick(id: String)

        /** Confirmed the delete confirmation. */
        fun onDeleteConfirm()

        /** Dismissed the delete confirmation with Cancel or outside it. */
        fun onDeleteDismiss()

        /** Chose Clear trip history in the bar's menu. */
        fun onClearClick()

        /** Confirmed the clear confirmation. */
        fun onClearConfirm()

        /** Dismissed the clear confirmation with Cancel or outside it. */
        fun onClearDismiss()

        /** Scrolled to the end of the drives shown, which asks for the next page. */
        fun onLoadMore()

        /** Tapped Try again after the list failed to load. */
        fun onRetry()

        /** Tapped the back button in the bar. */
        fun onClose()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onRateClick(id: String) = Unit
                override fun onDriveClick(id: String) = Unit
                override fun onPlanNewRoutesClick(id: String) = Unit
                override fun onRatingThumbClick(thumb: Thumb) = Unit
                override fun onRatingReasonClick(reason: RatingReason) = Unit
                override fun onRatingSheetClose() = Unit
                override fun onDeleteClick(id: String) = Unit
                override fun onDeleteConfirm() = Unit
                override fun onDeleteDismiss() = Unit
                override fun onClearClick() = Unit
                override fun onClearConfirm() = Unit
                override fun onClearDismiss() = Unit
                override fun onLoadMore() = Unit
                override fun onRetry() = Unit
                override fun onClose() = Unit
            }
        }
    }
}
