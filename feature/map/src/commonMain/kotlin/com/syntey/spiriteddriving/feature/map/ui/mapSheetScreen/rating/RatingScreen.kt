package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating

import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import kotlinx.coroutines.flow.StateFlow

/** The rating step of the map sheet, after a drive: its own state and taps, which the sheet's UI uses directly. */
interface RatingScreen {
    val viewState: StateFlow<RatingViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Good or Not good. */
        fun onThumbClick(thumb: Thumb)

        /** Tapped a reason chip. */
        fun onReasonClick(reason: RatingReason)

        /** Tapped Done. */
        fun onDoneClick()

        /** Tapped the close button in the header. */
        fun onCloseClick()

        /** Tapped I didn't drive it. */
        fun onNotDrivenClick()

        /** Tapped Turn on in the reminder offer. */
        fun onReminderOfferClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onThumbClick(thumb: Thumb) = Unit
                override fun onReasonClick(reason: RatingReason) = Unit
                override fun onDoneClick() = Unit
                override fun onCloseClick() = Unit
                override fun onNotDrivenClick() = Unit
                override fun onReminderOfferClick() = Unit
            }
        }
    }
}
