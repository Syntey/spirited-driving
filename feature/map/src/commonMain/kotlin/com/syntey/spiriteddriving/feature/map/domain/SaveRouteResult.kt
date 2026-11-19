package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute

/** How saving a route ended: saved, or not saved because its name is taken and a new one is needed. */
internal sealed interface SaveRouteResult {

    /** The route is saved; an already saved one comes back as it was. */
    data class Saved(val savedRoute: SavedRoute) : SaveRouteResult

    /**
     * The name is taken by another saved route. [suggestion] is a free name for the dialog, and [takenNames] are all
     * the names in use, for the dialog to check typed names against.
     */
    data class NameTaken(val takenName: String, val suggestion: String, val takenNames: List<String>) : SaveRouteResult
}
