package com.syntey.spiriteddriving.feature.map.model.ui

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
