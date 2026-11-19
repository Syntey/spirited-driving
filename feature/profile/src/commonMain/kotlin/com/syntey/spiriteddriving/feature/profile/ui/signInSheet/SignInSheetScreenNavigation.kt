package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import app.futured.arkitekt.decompose.navigation.NavigationActions
import com.syntey.spiriteddriving.feature.profile.model.SignInReason

interface SignInSheetScreenNavigation : NavigationActions {

    /**
     * Closes the sheet. [reason] is the one it was opened with; [isSignedIn] tells the host whether the feature that asked
     * for it can carry on.
     */
    fun SignInSheetScreen.finish(reason: SignInReason, isSignedIn: Boolean)
}
