package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileViewState
import dev.icerock.moko.resources.desc.desc

internal object ProfilePreviewStates {
    fun viewState(
        account: AccountRow? = null,
        signIn: SignInState = SignInState(),
    ) = ProfileViewState(account = account, signIn = signIn)

    fun signedInAccount() = AccountRow(supporting = "rider@example.com".desc())
}
