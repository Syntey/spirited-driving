package com.syntey.spiriteddriving.shared.navigation.profile

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreen
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreen
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreen
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreen
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetArgs
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreenNavigation
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreenNavigation

internal interface ProfileNavHostNavigation :
    ProfileScreenNavigation,
    SettingsScreenNavigation,
    FeedbackScreenNavigation,
    AboutScreenNavigation,
    AccountScreenNavigation,
    LicencesScreenNavigation,
    LicenceTextScreenNavigation,
    TripHistoryScreenNavigation,
    SignInSheetScreenNavigation {
    val stackNavigator: StackNavigation<ProfileConfig>
    val sheetNavigator: SlotNavigation<ProfileSheetConfig>
}

internal class ProfileNavHostNavigator(private val onFinished: (ProfileResult) -> Unit, private val onAccountDeleted: () -> Unit) :
    ProfileNavHostNavigation {
    override val stackNavigator: StackNavigation<ProfileConfig> = StackNavigation()
    override val sheetNavigator: SlotNavigation<ProfileSheetConfig> = SlotNavigation()

    override fun ProfileScreen.openSettings() = stackNavigator.pushNew(ProfileConfig.Settings)

    override fun ProfileScreen.openFeedback() = stackNavigator.pushNew(ProfileConfig.Feedback)

    override fun ProfileScreen.openAbout() = stackNavigator.pushNew(ProfileConfig.About)

    override fun ProfileScreen.openAccount() = stackNavigator.pushNew(ProfileConfig.Account)

    override fun ProfileScreen.openTripHistory() = stackNavigator.pushNew(ProfileConfig.TripHistory)

    // The sign-in sheet's only reason here is Trip history, so finishing it signed in opens that
    override fun ProfileScreen.openSignInForTripHistory() =
        sheetNavigator.activate(ProfileSheetConfig.SignIn(SignInSheetArgs(SignInReason.TripHistory)))

    override fun SignInSheetScreen.finish(reason: SignInReason, isSignedIn: Boolean) {
        sheetNavigator.dismiss()
        if (isSignedIn) stackNavigator.pushNew(ProfileConfig.TripHistory)
    }

    override fun TripHistoryScreen.pop() = stackNavigator.pop()

    override fun TripHistoryScreen.openOnMap(driveId: String) = onFinished(ProfileResult.ShowPastDrive(driveId))

    override fun TripHistoryScreen.planNewRoutes(driveId: String) = onFinished(ProfileResult.PlanNewRoutes(driveId))

    override fun AboutScreen.openLicences() = stackNavigator.pushNew(ProfileConfig.Licences)

    override fun AccountScreen.pop() = stackNavigator.pop()

    override fun AccountScreen.popAccountDeleted() = stackNavigator.pop { onAccountDeleted() }

    override fun LicencesScreen.openLicenceText(args: LicenceTextArgs) =
        stackNavigator.pushNew(ProfileConfig.LicenceText(args))

    override fun ProfileScreen.close() = onFinished(ProfileResult.Closed)

    override fun SettingsScreen.pop() = stackNavigator.pop()

    override fun FeedbackScreen.pop() = stackNavigator.pop()

    override fun AboutScreen.pop() = stackNavigator.pop()

    override fun LicencesScreen.pop() = stackNavigator.pop()

    override fun LicenceTextScreen.pop() = stackNavigator.pop()
}
