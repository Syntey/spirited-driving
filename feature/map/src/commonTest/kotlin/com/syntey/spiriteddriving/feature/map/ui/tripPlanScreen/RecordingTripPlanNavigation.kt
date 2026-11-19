package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

/** [TripPlanScreenNavigation] that records the screens the trip plan opened, in order. */
internal class RecordingTripPlanNavigation : TripPlanScreenNavigation {

    val calls = mutableListOf<Call>()

    override fun TripPlanScreen.openProfile() {
        calls += Call.Profile
    }

    override fun TripPlanScreen.openSignInToSaveRoute() {
        calls += Call.SignInToSaveRoute
    }

    override fun TripPlanScreen.openSavedRoutes() {
        calls += Call.SavedRoutes
    }

    override fun TripPlanScreen.openSignInForSavedRoutes() {
        calls += Call.SignInForSavedRoutes
    }

    enum class Call { Profile, SignInToSaveRoute, SavedRoutes, SignInForSavedRoutes }
}
