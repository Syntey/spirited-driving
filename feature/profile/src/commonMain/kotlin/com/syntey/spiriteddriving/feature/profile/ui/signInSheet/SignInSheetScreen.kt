package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import kotlinx.coroutines.flow.StateFlow

interface SignInSheetScreen {
    val viewState: StateFlow<SignInSheetViewState>
    val actions: Actions

    interface Actions {
        /** Tapped Continue with Google. */
        fun onGoogleClick()

        /** Tapped the close button, or dragged the sheet down. */
        fun onClose()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onGoogleClick() = Unit
                override fun onClose() = Unit
            }
        }
    }
}
