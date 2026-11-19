package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class SignInSheetViewState(
    val title: StringDesc = "".desc(),
    val isSigningIn: Boolean = false,
    /** The error of the last try, shown above the button. */
    val signInError: StringDesc? = null,
)
