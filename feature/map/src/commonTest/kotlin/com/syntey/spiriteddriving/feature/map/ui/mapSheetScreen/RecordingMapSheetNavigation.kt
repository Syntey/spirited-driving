package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

/** [MapSheetScreenNavigation] that records the screens the map sheet opened, in order. */
internal class RecordingMapSheetNavigation : MapSheetScreenNavigation {

    val calls = mutableListOf<Call>()

    override fun MapSheetScreen.openProfile() {
        calls += Call.Profile
    }

    override fun MapSheetScreen.openSignInToSaveRoute() {
        calls += Call.SignInToSaveRoute
    }

    override fun MapSheetScreen.openSavedRoutes() {
        calls += Call.SavedRoutes
    }

    override fun MapSheetScreen.openSignInForSavedRoutes() {
        calls += Call.SignInForSavedRoutes
    }

    enum class Call { Profile, SignInToSaveRoute, SavedRoutes, SignInForSavedRoutes }
}
