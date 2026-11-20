package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep

/**
 * What one route shows.
 *
 * @property step The one-route step: its origin, the route once loaded, and where loading is.
 */
internal data class OneRouteViewState(val step: OneRouteStep)
