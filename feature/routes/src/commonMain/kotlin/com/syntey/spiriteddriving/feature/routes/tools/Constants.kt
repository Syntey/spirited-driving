package com.syntey.spiriteddriving.feature.routes.tools

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal object Constants {

    object TripHistory {
        const val PAGE_SIZE = 30

        /** How long after a drive's hand-off the user can still rate it from the list. */
        val RATING_WINDOW: Duration = 7.days

        /** Placeholder rows while the first page loads, and while the next one does. */
        const val PLACEHOLDER_ROWS = 6
        const val LOADING_MORE_ROWS = 2

        /** How many rows from the end of the list, when the user is visible there, the next page is asked for. */
        const val NEAR_END_ROWS = 3
    }
}
