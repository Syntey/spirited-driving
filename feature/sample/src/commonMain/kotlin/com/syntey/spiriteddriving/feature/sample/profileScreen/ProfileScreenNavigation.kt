package com.syntey.spiriteddriving.feature.sample.profileScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface ProfileScreenNavigation : NavigationActions {
    fun ProfileScreen.navigateToLogin()
    fun ProfileScreen.navigateToThird(id: String)
}
