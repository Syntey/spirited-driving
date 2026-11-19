package com.syntey.spiriteddriving.shared.navigation.profile

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.Child
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreen
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreen
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetArgs
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreen
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Profile and the screens pushed from it, shown over the map: full screen on Android, a native sheet with its own
 * navigation stack on iOS.
 */
interface ProfileNavHost {
    val stack: StateFlow<ChildStack<ProfileConfig, ProfileChild>>

    /** Sheets over Profile, such as the sign-in sheet. */
    val sheet: StateFlow<ChildSlot<ProfileSheetConfig, ProfileSheetChild>>

    /** Android's back arrow and system back: the previous screen, or closes Profile from its first screen. */
    fun onBack()

    /** The iOS close button, or the iOS sheet swiped down. */
    fun onClose()

    /** The native iOS navigation stack popped to [newStack]. */
    fun iosPopTo(newStack: List<Child<ProfileConfig, ProfileChild>>)

    /** The sheet over Profile was swiped down or closed without finishing. */
    fun onSheetDismissed()
}

@Serializable
sealed interface ProfileConfig {

    @Serializable
    data object Profile : ProfileConfig

    @Serializable
    data object Settings : ProfileConfig

    @Serializable
    data object Feedback : ProfileConfig

    @Serializable
    data object About : ProfileConfig

    @Serializable
    data object Account : ProfileConfig

    @Serializable
    data object Licences : ProfileConfig

    @Serializable
    data class LicenceText(val args: LicenceTextArgs) : ProfileConfig

    @Serializable
    data object TripHistory : ProfileConfig
}

@Serializable
sealed interface ProfileSheetConfig {

    @Serializable
    data class SignIn(val args: SignInSheetArgs) : ProfileSheetConfig
}

sealed interface ProfileSheetChild {

    data class SignIn(val screen: SignInSheetScreen) : ProfileSheetChild
}

/** How Profile and the screens pushed from it finished: closed, or with a drive that the map should show. */
sealed interface ProfileResult {
    data object Closed : ProfileResult

    /** A drive chosen in Trip history, shown on the map. */
    data class ShowPastDrive(val driveId: String) : ProfileResult

    /** Plan new routes chosen in Trip history, for the drive's destination. */
    data class PlanNewRoutes(val driveId: String) : ProfileResult
}

sealed interface ProfileChild {

    data class Profile(val screen: ProfileScreen) : ProfileChild

    data class Settings(val screen: SettingsScreen) : ProfileChild

    data class Feedback(val screen: FeedbackScreen) : ProfileChild

    data class About(val screen: AboutScreen) : ProfileChild

    data class Account(val screen: AccountScreen) : ProfileChild

    data class Licences(val screen: LicencesScreen) : ProfileChild

    data class LicenceText(val screen: LicenceTextScreen) : ProfileChild

    data class TripHistory(val screen: TripHistoryScreen) : ProfileChild
}
