package com.syntey.spiriteddriving.core.platform.binding

/** How a sign-in with a provider ended, see [Auth.signIn]. */
sealed interface SignInResult {

    data object SignedIn : SignInResult

    data object Cancelled : SignInResult

    /** The email already has an account that signs in with [provider]. */
    data class AccountExistsWithOtherProvider(val provider: SignInProvider) : SignInResult

    data class Failed(val message: String) : SignInResult
}
