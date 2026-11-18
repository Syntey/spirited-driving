package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetViewState
import dev.icerock.moko.resources.desc.StringDesc

internal object SignInSheetPreviewStates {
    fun viewState(
        reason: SignInReason = SignInReason.SaveRoute,
        isSigningIn: Boolean = false,
        signInError: StringDesc? = null,
    ) = SignInSheetViewState(
        title = reason.title(),
        isSigningIn = isSigningIn,
        signInError = signInError,
    )
}
