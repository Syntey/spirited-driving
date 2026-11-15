package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

/** Back is drawn by the navigation hosts, so the screen has navigation only to the screens it opens. */
interface AboutScreenNavigation : NavigationActions {
    fun AboutScreen.openLicences()
}
