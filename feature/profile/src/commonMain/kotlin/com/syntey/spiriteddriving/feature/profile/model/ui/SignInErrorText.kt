package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.SignInResult
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

// A cancelled sign-in says nothing: the user chose to stop
internal fun signInErrorFor(result: SignInResult): StringDesc? = when (result) {
    SignInResult.SignedIn, SignInResult.Cancelled -> null
    is SignInResult.AccountExistsWithOtherProvider -> otherProviderError(result.provider)
    is SignInResult.Failed -> MR.strings.sign_in_failed.desc()
}

// The provider the email's account uses, so the message says which button to press
internal fun otherProviderError(provider: SignInProvider): StringDesc = when (provider) {
    SignInProvider.Google -> MR.strings.sign_in_other_provider_google.desc()
    SignInProvider.Apple -> MR.strings.sign_in_other_provider_apple.desc()
}
