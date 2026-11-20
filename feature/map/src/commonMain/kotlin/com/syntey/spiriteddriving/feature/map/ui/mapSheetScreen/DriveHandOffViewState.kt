package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog

/**
 * What Drive shows over the trip.
 *
 * @property isFirstDriveExplained Whether the user went past the first-drive dialog.
 * @property dialog The dialog about a drive; null when none is shown.
 */
internal data class DriveHandOffViewState(val isFirstDriveExplained: Boolean = false, val dialog: DriveDialog? = null)
