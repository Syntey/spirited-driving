package com.syntey.spiriteddriving.feature.profile.ui.licencesScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs

/** Back is drawn by the navigation hosts. */
interface LicencesScreenNavigation : NavigationActions {
    fun LicencesScreen.openLicenceText(args: LicenceTextArgs)
}
