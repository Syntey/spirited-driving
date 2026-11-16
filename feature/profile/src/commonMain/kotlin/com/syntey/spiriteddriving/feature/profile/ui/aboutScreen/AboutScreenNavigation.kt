package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface AboutScreenNavigation : NavigationActions {
    fun AboutScreen.pop()
    fun AboutScreen.openLicences()
}
