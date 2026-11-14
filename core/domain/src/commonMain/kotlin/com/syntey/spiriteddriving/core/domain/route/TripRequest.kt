package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.settings.CountryCode

/**
 * What the user asks to plan.
 *
 * @property start Where the trip starts. A start with `name = null` (the user's location) is named by the backend after
 * the nearest address.
 * @property vignettes Countries the user has a valid motorway vignette for; routes use only their vignette motorways.
 * @property maxHandOffWaypoints The most waypoints the target maps app accepts, counting the start.
 */
data class TripRequest(
    val start: Place,
    val destination: Place,
    val budget: Budget,
    val vignettes: Set<CountryCode>,
    val maxHandOffWaypoints: Int,
)
