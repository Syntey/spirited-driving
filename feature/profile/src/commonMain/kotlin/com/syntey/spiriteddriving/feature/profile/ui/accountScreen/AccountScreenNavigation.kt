package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

/**
 * Back is drawn by the navigation hosts. Signing out pops the screen too, once the account is gone. Deleting the account
 * pops it with [popAccountDeleted], so the screen before can say so.
 */
interface AccountScreenNavigation : NavigationActions {
    fun AccountScreen.pop()

    /** Back to Profile, which says the account is deleted. */
    fun AccountScreen.popAccountDeleted()
}
