package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess

/**
 * @property locationAccess What the user allows; null until it's read on start.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 */
data class TripPlanViewState(
    val locationAccess: LocationAccess? = null,
    val map: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val search: SearchState = SearchState(),
) {
    val locateButton: LocateButton get() = LocateButton.of(locationAccess, map.followsUser)

    /**
     * The "Precise location is off" note: with approximate access only, until the user closes it, and not while the user is
     * outside the covered area, whose note says more.
     */
    val showsPreciseLocationNote: Boolean
        get() = locationAccess == LocationAccess.Approximate && !isPreciseLocationNoteClosed && !map.isOutsideCoveredArea
}
