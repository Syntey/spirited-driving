package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.core.platform.binding.SignInResult
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/**
 * Where a sign-in on a screen stands: whether one is running and the error of the last try.
 *
 * @property isSigningIn True while a sign-in runs, so the button shows its loading state.
 * @property error The error of the last try, shown above the button.
 */
data class SignInState(val isSigningIn: Boolean = false, val error: StringDesc? = null) {
    companion object {
        /** A sign-in has started and its error is cleared. */
        val Running = SignInState(isSigningIn = true)

        /** The sign-in threw before it returned a result, so the generic error shows. */
        val Failed: SignInState
            get() = SignInState(error = MR.strings.sign_in_failed.desc())

        /** The state once a sign-in returned [result]. */
        fun after(result: SignInResult) = SignInState(error = signInErrorFor(result))
    }
}
