package com.syntey.spiriteddriving.feature.profile.model

import kotlinx.serialization.Serializable

/** What asked for the sign-in sheet, which sets its title. */
@Serializable
enum class SignInReason {
    /** Save in route planning */
    SaveRoute,

    /** The Saved routes row on the map */
    SavedRoutes,
}
