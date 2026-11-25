package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.measure.distanceText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import com.syntey.spiriteddriving.core.resources.measure.heightText
import dev.icerock.moko.resources.desc.StringDesc

/** A route as the route list shows it. */
data class RouteRow(
    val id: String,
    /** 1..3 for fun routes in list order; null for the fastest route. */
    val number: Int?,
    /** "+27 min" (time budget) or "+26 km" (distance budget); null for the fastest, which reads "Fastest". */
    val extra: StringDesc?,
    val duration: StringDesc,
    val distance: StringDesc,
    val curvyDistance: StringDesc,
    val ascent: StringDesc,
    val funLevel: Int,
    val isSelected: Boolean,
)

/** The route as a [RouteRow] in [distanceUnit]. */
internal fun routeRow(route: Route, distanceUnit: DistanceUnit, number: Int?, extra: StringDesc?, isSelected: Boolean): RouteRow =
    RouteRow(
        id = route.id,
        number = number,
        extra = extra,
        duration = durationText(route.duration),
        distance = distanceText(route.distance, distanceUnit, isApproximate = false),
        curvyDistance = distanceText(route.stats.curvyDistance, distanceUnit, isApproximate = false),
        ascent = heightText(route.stats.ascent, distanceUnit),
        funLevel = route.funLevel,
        isSelected = isSelected,
    )
