package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

/** A step that takes the sheet's place of the search and route planning, showing one route on the map. */
sealed interface SheetStep {
    /** The start pin the step puts on the map; null when it has none. */
    val start: GeoPoint?

    /** The destination pin the step puts on the map. */
    val destination: GeoPoint

    /** The area the map frames for the step. */
    val routeBounds: GeoBounds

    /** The step's one line on the map, or null when it has no points. */
    val routeLine: RouteLine?
}
