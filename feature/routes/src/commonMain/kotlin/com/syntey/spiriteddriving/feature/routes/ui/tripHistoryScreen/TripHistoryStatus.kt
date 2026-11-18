package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

/** What the list shows besides its rows. */
enum class TripHistoryStatus {
    /** Nothing is kept yet, and the first refresh runs. */
    Loading,

    /** The kept drives show, or the empty state when there are none. */
    Ready,

    /** Nothing is kept, and the phone has no connection to load them. */
    Offline,

    /** Nothing is kept, and loading failed for another reason. */
    Failed,
}
