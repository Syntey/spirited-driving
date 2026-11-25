package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.measure.durationText
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

/**
 * A saved route as its list row shows it.
 *
 * @property details "From Smíchov · 1 h 05 · 58 km", without the start when it has no area.
 * @property funLevel How fun the route is, from 1 to 5.
 */
data class SavedRouteRow(val id: SavedRouteId, val name: String, val details: StringDesc, val funLevel: Int)

/** This saved route as a row, with its distance in [distanceUnit]. */
fun SavedRoute.toRow(distanceUnit: DistanceUnit): SavedRouteRow {
    val duration = durationText(route.duration)
    val distance = distanceText(route.distance, distanceUnit, isApproximate = false)
    val area = route.start.shortArea
    val details = if (area == null) {
        StringDesc.ResourceFormatted(MR.strings.saved_routes_row_no_start, duration, distance)
    } else {
        StringDesc.ResourceFormatted(MR.strings.saved_routes_row, area, duration, distance)
    }
    return SavedRouteRow(id = id, name = name, details = details, funLevel = route.funLevel)
}
