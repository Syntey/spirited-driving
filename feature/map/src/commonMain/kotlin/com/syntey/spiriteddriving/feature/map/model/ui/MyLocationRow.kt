package com.syntey.spiriteddriving.feature.map.model.ui

/** The "My location" row, first in the start step, by what the user's location allows. */
enum class MyLocationRow {
    /** Precise and in the covered area: tapping it makes the user's location the start. */
    Available,

    /** Approximate access only: tapping it asks for precise location. */
    NeedsPreciseLocation,

    /** No location access: tapping it asks for access, or opens the system settings. */
    NeedsLocationAccess,

    /** The user is outside the covered area: dimmed, and tapping it explains routes stay in the covered area. */
    OutsideCoveredArea,
}
