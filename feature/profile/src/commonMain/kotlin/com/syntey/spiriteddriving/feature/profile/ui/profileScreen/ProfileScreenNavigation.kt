package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface ProfileScreenNavigation : NavigationActions {
    fun ProfileScreen.openSettings()
    fun ProfileScreen.openFeedback()
    fun ProfileScreen.openAbout()
    fun ProfileScreen.openAccount()

    /** Closes Profile and its screens. */
    fun ProfileScreen.close()
}
