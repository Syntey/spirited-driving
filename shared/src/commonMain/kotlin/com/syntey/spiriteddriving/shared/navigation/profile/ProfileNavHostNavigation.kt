package com.syntey.spiriteddriving.shared.navigation.profile

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreen
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreen
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenNavigation

internal interface ProfileNavHostNavigation :
    ProfileScreenNavigation,
    SettingsScreenNavigation,
    FeedbackScreenNavigation,
    AboutScreenNavigation,
    LicencesScreenNavigation,
    LicenceTextScreenNavigation {
    val stackNavigator: StackNavigation<ProfileConfig>
}

internal class ProfileNavHostNavigator : ProfileNavHostNavigation {
    override val stackNavigator: StackNavigation<ProfileConfig> = StackNavigation()

    override fun ProfileScreen.openSettings() = stackNavigator.pushNew(ProfileConfig.Settings)

    override fun ProfileScreen.openFeedback() = stackNavigator.pushNew(ProfileConfig.Feedback)

    override fun ProfileScreen.openAbout() = stackNavigator.pushNew(ProfileConfig.About)

    override fun AboutScreen.openLicences() = stackNavigator.pushNew(ProfileConfig.Licences)

    override fun LicencesScreen.openLicenceText(args: LicenceTextArgs) =
        stackNavigator.pushNew(ProfileConfig.LicenceText(args))

    override fun FeedbackScreen.pop() = stackNavigator.pop()
}
