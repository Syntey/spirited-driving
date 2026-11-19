package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.containsName
import com.syntey.spiriteddriving.core.domain.savedroute.isSavableName
import com.syntey.spiriteddriving.core.domain.tools.Constants
import kotlinx.collections.immutable.ImmutableList

/**
 * The dialog that renames a saved route.
 *
 * @property savedRouteId The saved route to rename.
 * @property currentName The name the field starts with, selected.
 * @property otherNames The names of the other saved routes, which a new name must not match.
 */
data class RenameDialog(val savedRouteId: String, val currentName: String, val otherNames: ImmutableList<String>) {

    /** The most characters the field takes. */
    val maxLength: Int get() = Constants.SavedRoutes.NAME_MAX_LENGTH

    /** Whether [name] may be saved: not blank, not longer than the maximum, changed, and not a name in use. */
    fun canSave(name: String): Boolean = isSavableName(name) && name.trim() != currentName && !isTaken(name)

    /** Whether [name] is the name of another saved route, compared trimmed and ignoring case. */
    fun isTaken(name: String): Boolean = otherNames.containsName(name)
}
