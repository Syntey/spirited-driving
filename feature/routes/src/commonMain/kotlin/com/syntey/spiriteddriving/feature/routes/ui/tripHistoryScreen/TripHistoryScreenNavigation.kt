package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface TripHistoryScreenNavigation : NavigationActions {
    fun TripHistoryScreen.pop()

    /** Shows the drive's route on the map, closing Profile. */
    fun TripHistoryScreen.openOnMap(driveId: String)

    /** Opens Route planning on the map to the drive's destination, closing Profile. */
    fun TripHistoryScreen.planNewRoutes(driveId: String)
}
