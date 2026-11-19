package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import com.syntey.spiriteddriving.feature.profile.model.ui.SignInState
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/**
 * @property title The sheet's title, from why it opened.
 * @property signIn Whether a sign-in runs and the error of the last try, shown above the button.
 */
data class SignInSheetViewState(val title: StringDesc = "".desc(), val signIn: SignInState = SignInState())
