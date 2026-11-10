package com.syntey.spiriteddriving.shared.navigation.signedIn

import app.futured.arkitekt.decompose.navigation.NavigationActions

internal interface SignedInNavHostNavigation : NavigationActions {
    fun SignedInNavHostComponent.navigateToLogin()
}
