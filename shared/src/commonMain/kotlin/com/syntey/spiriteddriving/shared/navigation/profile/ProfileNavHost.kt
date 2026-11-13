package com.syntey.spiriteddriving.shared.navigation.profile

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.Child
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreen
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

    /** Android's back arrow and system back: the previous screen, or closes Profile from its first screen. */
    fun onBack()

    /** The iOS close button, or the iOS sheet swiped down. */
    fun onClose()

    /** The native iOS navigation stack popped to [newStack]. */
    fun iosPopTo(newStack: List<Child<ProfileConfig, ProfileChild>>)
}

@Serializable
sealed interface ProfileConfig {

    @Serializable
    data object Profile : ProfileConfig

    @Serializable
    data object Settings : ProfileConfig

    @Serializable
    data object Feedback : ProfileConfig
}

sealed interface ProfileChild {

    data class Profile(val screen: ProfileScreen) : ProfileChild

    data class Settings(val screen: SettingsScreen) : ProfileChild

    data class Feedback(val screen: FeedbackScreen) : ProfileChild
}
