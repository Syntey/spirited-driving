package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProfileScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<ProfileViewState>
    val actions: Actions

    /** One-time events for the screen, such as opening a web page. */
    val events: Flow<ProfileUiEvent>

    /** Called by navigation when Account was left because the account was deleted. */
    fun onAccountDeleted()

    interface Actions {
        /** Tapped Settings. */
        fun onSettingsClick()

        /** Tapped Rate Spirited Driving. */
        fun onRateClick()

        /** Tapped Send feedback: opens the screen to write it. */
        fun onSendFeedbackClick()

        /** Tapped About Spirited Driving. */
        fun onAboutClick()

        /** Tapped Continue with Google on the sign-in card. */
        fun onSignInWithGoogleClick()

        /** Tapped the Account row, which shows the signed-in user. */
        fun onAccountClick()

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onSettingsClick() = Unit
                override fun onRateClick() = Unit
                override fun onSendFeedbackClick() = Unit
                override fun onAboutClick() = Unit
                override fun onSignInWithGoogleClick() = Unit
                override fun onAccountClick() = Unit
                override fun onBack() = Unit
            }
        }
    }
}
