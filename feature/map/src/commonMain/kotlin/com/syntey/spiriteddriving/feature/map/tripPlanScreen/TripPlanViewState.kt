package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * @property locationAccess What the user allows; null until it's read on start.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 * @property distanceUnit The unit distances are shown in.
 */
data class TripPlanViewState(
    val locationAccess: LocationAccess? = null,
    val map: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val search: SearchState = SearchState(),
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
) {
    val locateButton: LocateButton get() = LocateButton.of(locationAccess, map.followsUser)

    /**
     * The "Precise location is off" note: with approximate access only, until the user closes it, and not while the user is
     * outside the covered area, whose note says more.
     */
    val showsPreciseLocationNote: Boolean
        get() = locationAccess == LocationAccess.Approximate && !isPreciseLocationNoteClosed && !map.isOutsideCoveredArea

    /** The suggestions as the sheet shows them, in the same order. */
    val suggestionRows: ImmutableList<SuggestionRow>
        get() = search.suggestions.map { suggestion ->
            SuggestionRow(
                suggestion = suggestion,
                boldRanges = matchedRanges(name = suggestion.place.name, text = search.text),
                distance = distanceFrom(suggestion),
            )
        }.toImmutableList()

    // Straight-line, so it's known without a route; with approximate access it's only whole units
    private fun distanceFrom(suggestion: PlaceSuggestion): String? {
        val from = map.userLocation
        return if (from == null || !locationAccess.allowsLocation) {
            null
        } else {
            distanceText(
                metres = from.distanceTo(suggestion.place.location),
                unit = distanceUnit,
                isApproximate = locationAccess == LocationAccess.Approximate,
            )
        }
    }
}
