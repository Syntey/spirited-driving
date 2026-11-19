package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface TripPlanScreenNavigation : NavigationActions {

    /** Opens Profile over the map, from the map's profile button. */
    fun TripPlanScreen.openProfile()

    /** Opens the sign-in sheet for saving a route, from Save while signed out. */
    fun TripPlanScreen.openSignInToSaveRoute()

    /** Opens Saved routes over the map, from the Saved routes row while signed in. */
    fun TripPlanScreen.openSavedRoutes()

    /** Opens the sign-in sheet for the Saved routes row, from it while signed out. */
    fun TripPlanScreen.openSignInForSavedRoutes()
}
