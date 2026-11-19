package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId

interface SavedRoutesScreenNavigation : NavigationActions {
    fun SavedRoutesScreen.close()

    /** Shows the saved route on the map, in the sheet with the route alone. */
    fun SavedRoutesScreen.openOnMap(savedRouteId: SavedRouteId)
}
