package com.syntey.spiriteddriving.feature.profile.ui

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreen
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreen
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreen
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreen
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetArgs
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object ProfileScreens {

    fun profile(componentContext: AppComponentContext, navigation: ProfileScreenNavigation): ProfileScreen =
        ProfileComponentFactory.createComponent(componentContext, navigation)

    fun settings(componentContext: AppComponentContext, navigation: SettingsScreenNavigation): SettingsScreen =
        SettingsComponentFactory.createComponent(componentContext, navigation)

    fun feedback(componentContext: AppComponentContext, navigation: FeedbackScreenNavigation): FeedbackScreen =
        FeedbackComponentFactory.createComponent(componentContext, navigation)

    fun about(componentContext: AppComponentContext, navigation: AboutScreenNavigation): AboutScreen =
        AboutComponentFactory.createComponent(componentContext, navigation)

    fun account(componentContext: AppComponentContext, navigation: AccountScreenNavigation): AccountScreen =
        AccountComponentFactory.createComponent(componentContext, navigation)

    fun licences(componentContext: AppComponentContext, navigation: LicencesScreenNavigation): LicencesScreen =
        LicencesComponentFactory.createComponent(componentContext, navigation)

    fun licenceText(
        componentContext: AppComponentContext,
        navigation: LicenceTextScreenNavigation,
        args: LicenceTextArgs,
    ): LicenceTextScreen = LicenceTextComponentFactory.createComponent(componentContext, navigation, args)

    fun signInSheet(
        componentContext: AppComponentContext,
        navigation: SignInSheetScreenNavigation,
        args: SignInSheetArgs,
    ): SignInSheetScreen = SignInSheetComponentFactory.createComponent(componentContext, navigation, args)
}
