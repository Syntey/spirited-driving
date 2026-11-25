package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * What the map sheet owns: the sheet's height, the distance unit, the location and the map, Drive, Save and the drive question.
 * The steps' state is projected into [MapSheetViewState] instead.
 *
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 * @property distanceUnit The unit distances are shown in.
 * @property locationAccess What the user allows; null until it's read on start.
 * @property baseMap The map from the user's location, the coverage and following, without pins.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property isFirstDriveExplained Whether the user went past the first-drive dialog.
 * @property driveDialog The dialog about a drive; null when none is shown.
 * @property reminder The reminder about the drive not rated yet; null when there's none.
 * @property isSignedIn Whether a user is signed in; Save needs one.
 * @property savedRoutes The kept saved routes, which the selected route is checked against.
 * @property isSavingRoute Whether a route is being saved or removed; Save shows its progress meanwhile.
 * @property nameDialog The dialog asking for a name, shown when the destination's name is taken; null when there's none.
 */
internal data class MapSheetState(
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val locationAccess: LocationAccess? = null,
    val baseMap: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val isFirstDriveExplained: Boolean = false,
    val driveDialog: DriveDialog? = null,
    val reminder: ReminderBubble? = null,
    val isSignedIn: Boolean = false,
    val savedRoutes: ImmutableList<SavedRoute> = persistentListOf(),
    val isSavingRoute: Boolean = false,
    val nameDialog: RouteNameDialog? = null,
)
