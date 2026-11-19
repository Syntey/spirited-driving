package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetViewState

internal object SignInSheetPreviewStates {
    fun viewState(
        reason: SignInReason = SignInReason.SaveRoute,
        signIn: SignInState = SignInState(),
    ) = SignInSheetViewState(
        title = reason.title(),
        signIn = signIn,
    )
}
