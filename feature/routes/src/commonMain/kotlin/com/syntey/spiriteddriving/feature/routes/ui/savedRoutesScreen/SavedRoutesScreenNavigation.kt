package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface SavedRoutesScreenNavigation : NavigationActions {
    fun SavedRoutesScreen.close()

    /** Shows the saved route on the map, in the sheet with the route alone. */
    fun SavedRoutesScreen.openOnMap(savedRouteId: String)
}
