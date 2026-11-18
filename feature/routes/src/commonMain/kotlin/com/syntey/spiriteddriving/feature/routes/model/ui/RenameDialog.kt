package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.SAVED_ROUTE_NAME_MAX_LENGTH
import kotlinx.collections.immutable.ImmutableList

/**
 * The dialog that renames a saved route.
 *
 * @property savedRouteId The saved route to rename.
 * @property currentName The name the field starts with, selected.
 * @property otherNames The names of the other saved routes, which a new name must not match.
 */
data class RenameDialog(val savedRouteId: String, val currentName: String, val otherNames: ImmutableList<String>) {

    /** Whether [name] may be saved: not blank, not longer than the maximum, changed, and not a name in use. */
    fun canSave(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= SAVED_ROUTE_NAME_MAX_LENGTH && trimmed != currentName && !isTaken(name)
    }

    /** Whether [name] is the name of another saved route, compared trimmed and ignoring case. */
    fun isTaken(name: String): Boolean {
        val key = name.trim().lowercase()
        return otherNames.any { it.trim().lowercase() == key }
    }
}
