package com.syntey.spiriteddriving.core.domain.place

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

/**
 * A place a trip can start or end at.
 *
 * @property area Where it is, shown under its name, such as the town and region; null when there's none.
 */
data class Place(val name: String, val area: String?, val location: GeoPoint)
