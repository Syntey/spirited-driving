package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight

/**
 * What the map sheet itself owns: the location and the map it shows, the sheet's height, the precise location note, the distance
 * unit and the reminder. The steps' state and the dialogs of Drive and Save are projected into [MapSheetViewState] instead.
 *
 * @property baseMap The map from the component's own location, coverage and following, without pins.
 * @property locationAccess What the user allows; null until it's read on start.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 * @property distanceUnit The unit distances are shown in.
 * @property reminder The reminder about the drive not rated yet; null when there's none.
 */
internal data class MapSheetState(
    val locationAccess: LocationAccess? = null,
    val baseMap: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val reminder: ReminderBubble? = null,
)
