package com.syntey.spiriteddriving.feature.navigation.signedIn

import app.futured.arkitekt.decompose.navigation.NavigationActions

internal interface SignedInNavHostNavigation : NavigationActions {
    fun SignedInNavHostComponent.navigateToLogin()
}
