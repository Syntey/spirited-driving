package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface SignInSheetScreenNavigation : NavigationActions {

    /** Closes the sheet. [isSignedIn] tells the host whether the feature that asked for it can carry on. */
    fun SignInSheetScreen.finish(isSignedIn: Boolean)
}
