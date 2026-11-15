package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

/** Back is drawn by the navigation hosts. Signing out pops the screen too, once the account is gone. */
interface AccountScreenNavigation : NavigationActions {
    fun AccountScreen.pop()
}
