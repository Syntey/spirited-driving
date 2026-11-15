package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountViewState

internal object AccountPreviewStates {
    fun googleViewState() = AccountViewState(
        name = "Alex Driver",
        email = "rider@example.com",
        provider = SignInProvider.Google,
    )

    // A Hide My Email address, and no name: the user chose not to share either
    fun appleViewState() = AccountViewState(
        email = "x7k2@privaterelay.appleid.com",
        provider = SignInProvider.Apple,
    )
}
