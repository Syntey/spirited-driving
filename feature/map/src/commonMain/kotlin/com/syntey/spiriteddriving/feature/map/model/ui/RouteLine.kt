package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList

/**
 * A route as the map draws it: its line, and the label on the line.
 *
 * @property number 1..3 for fun routes in list order; null for the fastest route. Same numbering as [RouteRow].
 * @property label "1 · +27 min" for a fun route, "Fastest · 38 min" for the fastest; null for a line drawn without a label, as the
 *   rating step's.
 * @property labelPoint Where the label sits: halfway along [points].
 */
data class RouteLine(
    val id: String,
    val number: Int?,
    val points: ImmutableList<GeoPoint>,
    val label: StringDesc?,
    val labelPoint: GeoPoint,
    val isSelected: Boolean,
)
