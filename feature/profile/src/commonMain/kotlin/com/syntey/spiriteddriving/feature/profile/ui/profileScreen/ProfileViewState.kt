package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import com.syntey.spiriteddriving.feature.profile.model.ui.AccountRow
import dev.icerock.moko.resources.desc.StringDesc

data class ProfileViewState(
    /** Null when signed out, and the sign-in card shows. */
    val account: AccountRow? = null,
    val isSigningIn: Boolean = false,
    val signInError: StringDesc? = null,
)
