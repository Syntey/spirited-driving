package com.syntey.spiriteddriving.feature.sample.loginScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface LoginScreenNavigation : NavigationActions {
    fun LoginScreen.navigateToSignedIn()
}
