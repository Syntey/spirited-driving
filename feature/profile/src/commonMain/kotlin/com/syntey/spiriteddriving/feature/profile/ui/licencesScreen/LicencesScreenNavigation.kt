package com.syntey.spiriteddriving.feature.profile.ui.licencesScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs

interface LicencesScreenNavigation : NavigationActions {
    fun LicencesScreen.pop()
    fun LicencesScreen.openLicenceText(args: LicenceTextArgs)
}
