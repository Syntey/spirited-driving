package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint

/**
 * The first point of [points] at or past half of the line's length, measured along the line, not by point count. The
 * point is a vertex of the line, so it's on the route.
 */
internal fun midpointAlong(points: List<GeoPoint>): GeoPoint {
    require(points.isNotEmpty()) { "A line has at least one point" }
    val half = points.zipWithNext { from, to -> from.distanceTo(to).inMetres }.sum() / 2
    var walked = 0.0
    points.zipWithNext().forEach { (from, to) ->
        walked += from.distanceTo(to).inMetres
        if (walked >= half) return to
    }
    return points.last()
}
