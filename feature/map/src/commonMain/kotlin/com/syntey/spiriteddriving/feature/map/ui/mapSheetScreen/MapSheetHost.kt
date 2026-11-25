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

    /** How far the sheet is pulled up now. */
    val sheetHeight: SheetHeight

    /** Asks for the sheet to be pulled to [height]. */
    fun requestSheetHeight(height: SheetHeight)

    /** The step on top is done or closed: it's popped, and the sheet goes to [sheetHeight] over the search it leaves. */
    fun onStepClosed(sheetHeight: SheetHeight = SheetHeight.Collapsed)

    /** Whether [point] is in the covered area. */
    fun covers(point: GeoPoint): Boolean

    /** Shows that the chosen place is outside the covered area. */
    fun onNotCovered()

    /** A place was chosen in a search: the destination, or in planning's start search the start. */
    fun onPlaceChosen(place: Place)

    /** The destination search keeps [place] with the recent places: a chosen start, or a picked point once it's named. */
    fun keepRecentPlace(place: Place)

    /** The recent places changed; every place search shows them. */
    fun onRecentPlacesChanged(places: List<Place>)
}
