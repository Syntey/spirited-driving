package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight

/**
 * What the sheet's steps reach on the parent, which implements it. Steps never see the parent's other children.
 */
internal interface MapSheetHost {
    /** The user's location, once it's known. */
    val userLocation: GeoPoint?

    /** Whether the start search is open, which route planning waits on for its start. */
    val isStartSearchOpen: Boolean

    /** Asks for the sheet to be pulled to [height]. */
    fun requestSheetHeight(height: SheetHeight)

    /** Whether [point] is in the covered area. */
    fun covers(point: GeoPoint): Boolean

    /** Shows that the chosen place is outside the covered area. */
    fun onNotCovered()

    /** A place was chosen: a destination, or the start while the start search is open. */
    fun onPlaceChosen(place: Place)

    /** A picked point was named, or failed to be named with its coordinates; the destination search keeps the place. */
    fun onPlaceNamed(place: Place)

    /** The recent places changed; every place search shows them. */
    fun onRecentPlacesChanged(places: List<Place>)
}
