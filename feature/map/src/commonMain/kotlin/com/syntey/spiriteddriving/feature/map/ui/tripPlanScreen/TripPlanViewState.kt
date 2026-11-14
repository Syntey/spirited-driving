package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

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
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.SuggestionRow
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.tools.distanceText
import com.syntey.spiriteddriving.feature.map.tools.durationText
import com.syntey.spiriteddriving.feature.map.tools.heightText
import com.syntey.spiriteddriving.feature.map.tools.matchedRanges
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
}
