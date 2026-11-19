package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import com.syntey.spiriteddriving.feature.profile.model.ui.AccountRow
import com.syntey.spiriteddriving.feature.profile.model.ui.SignInState

/**
 * @property account Null when signed out, and the sign-in card shows.
 * @property signIn The sign-in card's state: whether a sign-in runs and the error of the last try.
 */
data class ProfileViewState(val account: AccountRow? = null, val signIn: SignInState = SignInState())
