package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.RecentPlaceRow
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.RouteLine
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.SuggestionRow
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.tools.distanceText
import com.syntey.spiriteddriving.feature.map.tools.durationText
import com.syntey.spiriteddriving.feature.map.tools.heightText
import com.syntey.spiriteddriving.feature.map.tools.matchedRanges
import com.syntey.spiriteddriving.feature.map.tools.midpointAlong
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * @property locationAccess What the user allows; null until it's read on start.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 * @property distanceUnit The unit distances are shown in.
 * @property planning The route planning step; null while the sheet shows the search.
 */
data class TripPlanViewState(
    val locationAccess: LocationAccess? = null,
    val map: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val search: SearchState = SearchState(),
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val planning: RoutePlanningState? = null,
) {
    val locateButton: LocateButton get() = LocateButton.of(locationAccess, map.followsUser)

    /** The profile and locate buttons hide while planning, as the planned routes take the map. */
    val showsMapButtons: Boolean get() = planning == null

    /**
     * The area the map frames: all the planned routes' lines once they have points; otherwise, and while planning, the user's
     * location, when known, and the destination. Never smaller than the minimum span, so a single point still shows its streets.
     * Null without planning.
     */
    val routeBounds: GeoBounds?
        get() {
            val planning = planning ?: return null
            val points = if (planning.plan != null) planning.listedRoutes.flatMap { it.line } else emptyList()
            val bounds = if (points.isEmpty()) planningBounds(planning) else boundsOf(points)
            return bounds.withMinimumSpan()
        }

    /**
     * Route planning starts from the user's location. Without it, choosing a place only drops its pin until the start can be
     * searched for.
     */
    val canPlanFromUser: Boolean get() = locationAccess.allowsLocation && map.userLocation != null

    /**
     * The "Precise location is off" note: with approximate access only, until the user closes it, and not while the user is
     * outside the covered area, whose note says more.
     */
    val showsPreciseLocationNote: Boolean
        get() = locationAccess == LocationAccess.Approximate && !isPreciseLocationNoteClosed && !map.isOutsideCoveredArea

    /** The recent places as the sheet shows them, in the same order. */
    val recentRows: ImmutableList<RecentPlaceRow>
        get() = search.shownRecentPlaces.map { place ->
            RecentPlaceRow(
                place = place,
                isCovered = map.covers(place.location),
                boldRanges = matchedRanges(name = place.name, text = search.text),
                distance = distanceFrom(place.location),
            )
        }.toImmutableList()

    /** The suggestions as the sheet shows them, in the same order. */
    val suggestionRows: ImmutableList<SuggestionRow>
        get() = search.shownSuggestions.map { suggestion ->
            SuggestionRow(
                suggestion = suggestion,
                boldRanges = matchedRanges(name = suggestion.place.name, text = search.text),
                distance = distanceFrom(suggestion.place.location),
            )
        }.toImmutableList()

    /** The routes of the plan as the route list shows them; empty without a plan. */
    val routeRows: ImmutableList<RouteRow>
        get() {
            val planning = planning ?: return persistentListOf()
            // The fun routes come first in the listed routes, so a fun route's number is its place in the list
            return planning.listedRoutes.mapIndexed { index, route ->
                RouteRow(
                    id = route.id,
                    number = (index + 1).takeIf { route.kind == RouteKind.Fun },
                    extra = extraText(route, planning.budget),
                    duration = durationText(route.duration),
                    distance = distanceText(route.distance, distanceUnit, isApproximate = false),
                    curvyDistance = distanceText(route.stats.curvyDistance, distanceUnit, isApproximate = false),
                    ascent = heightText(route.stats.ascent, distanceUnit),
                    funLevel = route.funLevel,
                    isSelected = route.id == planning.selectedRouteId,
                )
            }.toImmutableList()
        }

    /**
     * The routes' lines as the map draws them, each with its label; empty without a plan. The selected line is last, so it
     * draws on top of the others.
     */
    val routeLines: ImmutableList<RouteLine>
        get() {
            val planning = planning ?: return persistentListOf()
            return planning.listedRoutes.mapIndexedNotNull { index, route ->
                // A line without points has nothing to draw, and no midpoint
                if (route.line.isEmpty()) return@mapIndexedNotNull null
                // Same numbering as routeRows
                val number = (index + 1).takeIf { route.kind == RouteKind.Fun }
                val points = route.line.toImmutableList()
                RouteLine(
                    id = route.id,
                    number = number,
                    points = points,
                    label = if (number == null) {
                        StringDesc.ResourceFormatted(MR.strings.planning_fastest_label, durationText(route.duration))
                    } else {
                        StringDesc.ResourceFormatted(
                            MR.strings.planning_route_label,
                            number,
                            requireNotNull(extraText(route, planning.budget)) { "A fun route has an extra over the fastest" },
                        )
                    },
                    labelPoint = midpointAlong(route.line),
                    isSelected = route.id == planning.selectedRouteId,
                )
            }.sortedBy { it.isSelected }.toImmutableList()
        }

    private fun planningBounds(planning: RoutePlanningState): GeoBounds {
        val destination = planning.destination.location
        val origin = map.userLocation ?: return GeoBounds(destination, destination)
        return GeoBounds(origin, origin).including(destination)
    }

    // Grows around the centre, so a short or narrow area is framed with the streets around it
    private fun GeoBounds.withMinimumSpan(): GeoBounds {
        val latSpan = northEast.latitude - southWest.latitude
        val lonSpan = northEast.longitude - southWest.longitude
        if (latSpan >= MIN_LAT_SPAN && lonSpan >= MIN_LON_SPAN) return this
        val centreLat = (northEast.latitude + southWest.latitude) / 2
        val centreLon = (northEast.longitude + southWest.longitude) / 2
        val halfLat = maxOf(latSpan, MIN_LAT_SPAN) / 2
        val halfLon = maxOf(lonSpan, MIN_LON_SPAN) / 2
        return GeoBounds(
            southWest = GeoPoint(latitude = centreLat - halfLat, longitude = centreLon - halfLon),
            northEast = GeoPoint(latitude = centreLat + halfLat, longitude = centreLon + halfLon),
        )
    }

    // The fastest route is the reference, so it has no extra over itself
    private fun extraText(route: Route, budget: Budget): StringDesc? {
        if (route.kind == RouteKind.Fastest) return null
        return when (budget) {
            is Budget.Time -> StringDesc.ResourceFormatted(MR.strings.planning_extra, durationText(route.extraDuration))

            is Budget.Distance -> StringDesc.ResourceFormatted(
                MR.strings.planning_extra,
                distanceText(route.extraDistance, distanceUnit, isApproximate = false),
            )
        }
    }

    // Straight-line, so it's known without a route; with approximate access it's only whole units
    private fun distanceFrom(location: GeoPoint): StringDesc? {
        val from = map.userLocation
        return if (from == null || !locationAccess.allowsLocation) {
            null
        } else {
            distanceText(
                distance = from.distanceTo(location),
                unit = distanceUnit,
                isApproximate = locationAccess == LocationAccess.Approximate,
            )
        }
    }

    private companion object {
        // About a kilometre each way, as the destination's pin is framed
        const val MIN_LAT_SPAN = 0.02
        const val MIN_LON_SPAN = 0.03
    }
}

private fun boundsOf(points: List<GeoPoint>): GeoBounds =
    points.drop(1).fold(GeoBounds(points.first(), points.first())) { bounds, point -> bounds.including(point) }
