package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import kotlinx.serialization.Serializable

@Serializable
data class SignInSheetArgs(val reason: SignInReason)
