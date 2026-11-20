package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * What Save shows over the trip.
 *
 * @property isSignedIn Whether a user is signed in; Save needs one.
 * @property savedRoutes The kept saved routes, which the selected route is checked against.
 * @property isSaving Whether a route is being saved or removed; Save shows its progress meanwhile.
 * @property nameDialog The dialog asking for a name, shown when the destination's name is taken; null when there's none.
 */
internal data class RouteSavingViewState(
    val isSignedIn: Boolean = false,
    val savedRoutes: ImmutableList<SavedRoute> = persistentListOf(),
    val isSaving: Boolean = false,
    val nameDialog: RouteNameDialog? = null,
)
