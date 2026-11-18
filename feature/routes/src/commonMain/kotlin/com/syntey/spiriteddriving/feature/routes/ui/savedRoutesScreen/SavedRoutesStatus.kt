package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

/** What the list shows besides its rows. */
enum class SavedRoutesStatus {
    /** Nothing is kept yet, and the first refresh runs. */
    Loading,

    /** The kept routes show, or the empty state when there are none. */
    Ready,

    /** Nothing is kept, and the phone has no connection to load them. */
    Offline,

    /** Nothing is kept, and loading failed for another reason. */
    Failed,
}
