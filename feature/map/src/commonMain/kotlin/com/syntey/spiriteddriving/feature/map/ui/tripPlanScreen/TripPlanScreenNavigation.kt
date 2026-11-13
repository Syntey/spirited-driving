package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface TripPlanScreenNavigation : NavigationActions {

    /** Opens Profile over the map, from the map's profile button. */
    fun TripPlanScreen.openProfile()
}
