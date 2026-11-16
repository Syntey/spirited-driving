package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AccountScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<AccountViewState>
    val actions: Actions

    /** One-time events for the screen, such as a failed deletion. */
    val events: Flow<AccountUiEvent>

    interface Actions {
        /** Tapped Sign out. */
        fun onSignOutClick()

        /** Tapped Delete account: asks for confirmation first. */
        fun onDeleteAccountClick()

        /** Confirmed Delete account in the dialog. */
        fun onDeleteConfirm()

        /** Dismissed the delete dialog with Cancel or outside it. */
        fun onDeleteDialogDismiss()

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onSignOutClick() = Unit
                override fun onDeleteAccountClick() = Unit
                override fun onDeleteConfirm() = Unit
                override fun onDeleteDialogDismiss() = Unit
                override fun onBack() = Unit
            }
        }
    }
}
