package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface ProfileScreenNavigation : NavigationActions {
    fun ProfileScreen.openSettings()
    fun ProfileScreen.openFeedback()
    fun ProfileScreen.openAbout()
    fun ProfileScreen.openAccount()

    /** Opens Trip history, for a signed-in user. */
    fun ProfileScreen.openTripHistory()

    /** Asks a signed-out user to sign in, then opens Trip history. */
    fun ProfileScreen.openSignInForTripHistory()

    /** Closes Profile and its screens. */
    fun ProfileScreen.close()
}
