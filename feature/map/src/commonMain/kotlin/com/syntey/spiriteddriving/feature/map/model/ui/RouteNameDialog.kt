package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.containsName
import com.syntey.spiriteddriving.core.domain.savedroute.isSavableName
import com.syntey.spiriteddriving.core.domain.tools.Constants
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

    /** The most characters the field takes. */
    val maxLength: Int get() = Constants.SavedRoutes.NAME_MAX_LENGTH

    /** Whether [name] may be saved: not blank, not longer than the maximum, and not a name in use. */
    fun canSave(name: String): Boolean = isSavableName(name) && !isTaken(name)

    /** Whether [name] is a name in use, compared trimmed and ignoring case. */
    fun isTaken(name: String): Boolean = takenNames.containsName(name)
}
