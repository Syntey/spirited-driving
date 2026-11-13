package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface ProfileScreenNavigation : NavigationActions {
    fun ProfileScreen.openSettings()
}
