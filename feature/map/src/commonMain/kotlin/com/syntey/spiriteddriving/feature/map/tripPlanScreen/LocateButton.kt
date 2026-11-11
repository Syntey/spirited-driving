package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess

/**
 * What the locate button shows, and so what tapping it does.
 */
enum class LocateButton {
    /** The map follows the user; tapping does nothing. */
    Following,

    /** The user moved the map away; tapping recenters it and follows again. */
    NotFollowing,

    /** No location access; tapping asks for it, or opens the system settings once the system won't ask again. */
    NoAccess,
    ;

    companion object {
        /** The button for [access], or for access not read yet when it's null. */
        fun of(access: LocationAccess?, isFollowing: Boolean): LocateButton =
            if (access.allowsLocation) {
                if (isFollowing) Following else NotFollowing
            } else {
                NoAccess
            }
    }
}

internal val LocationAccess?.allowsLocation: Boolean
    get() = this == LocationAccess.Precise || this == LocationAccess.Approximate
