package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileViewState
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

internal object ProfilePreviewStates {
    fun viewState(
        account: AccountRow? = null,
        isSigningIn: Boolean = false,
        signInError: StringDesc? = null,
    ) = ProfileViewState(account = account, isSigningIn = isSigningIn, signInError = signInError)

    fun signedInAccount() = AccountRow(supporting = "rider@example.com".desc())

    fun signInError() = MR.strings.sign_in_failed.desc()
}
