package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.SAVED_ROUTE_NAME_MAX_LENGTH
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList

/**
 * The dialog that asks for a name, as the destination's name is already taken by a saved route.
 *
 * @property routeId The planned route to save once a name is given.
 * @property takenName The name that is taken.
 * @property suggestion The name the field starts with.
 * @property takenNames All names in use, which a typed name must not match.
 */
data class RouteNameDialog(val routeId: String, val takenName: String, val suggestion: String, val takenNames: ImmutableList<String>) {
    /** "You already have a route called Karlštejn Castle." */
    val message: StringDesc get() = StringDesc.ResourceFormatted(MR.strings.saved_routes_name_taken_message, takenName)

    /** Whether [name] may be saved: not blank, not longer than the maximum, and not a name in use. */
    fun canSave(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= SAVED_ROUTE_NAME_MAX_LENGTH && !isTaken(name)
    }

    /** Whether [name] is a name in use, compared trimmed and ignoring case. */
    fun isTaken(name: String): Boolean {
        val key = name.trim().lowercase()
        return takenNames.any { it.trim().lowercase() == key }
    }
}
