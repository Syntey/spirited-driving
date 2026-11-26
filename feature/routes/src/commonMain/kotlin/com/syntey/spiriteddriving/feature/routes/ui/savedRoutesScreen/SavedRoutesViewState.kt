package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.routes.model.ui.RenameDialog
import com.syntey.spiriteddriving.feature.routes.model.ui.SavedRouteRow
import com.syntey.spiriteddriving.feature.routes.model.ui.SavedRoutesStatus
import com.syntey.spiriteddriving.feature.routes.model.ui.toRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

data class SavedRoutesViewState(
    /** The kept saved routes, newest first, or `null` while nothing is kept. */
    val savedRoutes: ImmutableList<SavedRoute>? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val status: SavedRoutesStatus = SavedRoutesStatus.Loading,
    val renameDialog: RenameDialog? = null,
    /** The route whose delete confirmation is shown, or `null`. */
    val deleteConfirmationId: SavedRouteId? = null,
    /** Routes whose delete is being sent: they're left out of [routes] until it's done, and come back if it fails. */
    val deletingIds: ImmutableSet<SavedRouteId> = persistentSetOf(),
) {
    /** The rows to show, without the routes whose delete is being sent. */
    val routes: ImmutableList<SavedRouteRow>
        get() = savedRoutes.orEmpty()
            .filterNot { it.id in deletingIds }
            .map { it.toRow(distanceUnit) }
            .toImmutableList()

    /** Whether nothing is kept. A route whose delete is being sent still counts, so the list doesn't flash empty. */
    val isEmpty: Boolean
        get() = savedRoutes.isNullOrEmpty()

    /** Whether the delete confirmation is shown. */
    val isDeleteConfirmationShown: Boolean get() = deleteConfirmationId != null
}
