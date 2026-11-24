package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute

import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.datetime.driveDateTimeText
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import com.syntey.spiriteddriving.feature.map.model.ui.routeRow
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

/**
 * What one route shows.
 *
 * @property step The one-route step: its origin, the route once loaded, and where loading is.
 * @property distanceUnit The unit the route's distances are shown in.
 */
data class OneRouteViewState(val step: OneRouteStep, val distanceUnit: DistanceUnit = DistanceUnit.Kilometres) {
    /** The route's summary row, numbered 1 without an extra; null until the route is loaded. */
    val row: RouteRow?
        get() {
            val route = step.route ?: return null
            return routeRow(route = route, distanceUnit = distanceUnit, number = 1, extra = null, isSelected = true)
        }

    /** The header title: the saved route's name, or the past drive's destination. */
    val title: String
        get() = when (val origin = step.origin) {
            is OneRouteOrigin.Saved -> origin.savedRoute.name
            is OneRouteOrigin.Past -> origin.destination.name ?: origin.destination.area.orEmpty()
        }

    /**
     * The header subtitle: the start's area, and for a past drive, when it started before that. Null for a saved route
     * without the start's area.
     */
    val subtitle: StringDesc?
        get() = when (val origin = step.origin) {
            is OneRouteOrigin.Saved -> origin.start.shortArea?.let { StringDesc.ResourceFormatted(MR.strings.planning_from, it) }

            is OneRouteOrigin.Past -> {
                val started = driveDateTimeText(origin.drive.startedAt)
                origin.start.shortArea?.let { StringDesc.ResourceFormatted(MR.strings.map_past_drive_subtitle, started, it) }
                    ?: StringDesc.ResourceFormatted(MR.strings.map_past_drive_subtitle_no_start, started)
            }
        }
}
