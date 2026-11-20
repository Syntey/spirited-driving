package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface MapSheetScreenNavigation : NavigationActions {

    /** Opens Profile over the map, from the map's profile button. */
    fun MapSheetScreen.openProfile()

    /** Opens the sign-in sheet for saving a route, from Save while signed out. */
    fun MapSheetScreen.openSignInToSaveRoute()

    /** Opens Saved routes over the map, from the Saved routes row while signed in. */
    fun MapSheetScreen.openSavedRoutes()

    /** Opens the sign-in sheet for the Saved routes row, from it while signed out. */
    fun MapSheetScreen.openSignInForSavedRoutes()
}
