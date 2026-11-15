package com.syntey.spiriteddriving.core.platform.binding

/**
 * The Firebase sign-in and the provider's sign-in sheet (Google or Apple). The app reads and changes the signed-in user
 * through this; the backend gets the Firebase ID token it issues, see [Auth].
 */
interface PlatformAuth {

    /** The signed-in user, or null when signed out. Can be called from any thread. */
    fun currentUser(): AuthUser?

    /**
     * Calls [listener] with the current user at once and on every sign-in and sign-out, until the returned registration
     * is removed. Can be called from any thread; [listener] may be called on any thread.
     */
    fun addUserListener(listener: AuthUserListener): AuthListenerRegistration

    /**
     * Shows the provider's sign-in UI and signs in to Firebase with the result. Exactly one method of [listener] is called,
     * on any thread. Can be called from any thread.
     */
    fun signIn(provider: SignInProvider, listener: SignInListener)

    /** Signs out of Firebase and the provider. Can be called from any thread. */
    fun signOut()

    /**
     * Reports the Firebase ID token to [listener], refreshed by the SDK when it is about to expire. Reports null when signed
     * out or when no token can be had. Can be called from any thread; [listener] may be called on any thread.
     */
    fun idToken(listener: TokenListener)
}

enum class SignInProvider { Google, Apple }

/** @property name and email are null when the provider didn't share them. */
data class AuthUser(val id: String, val name: String?, val email: String?, val provider: SignInProvider)

fun interface AuthUserListener {
    fun onUser(user: AuthUser?)
}

fun interface AuthListenerRegistration {
    /** Stops the listener from being called. */
    fun remove()
}

fun interface TokenListener {
    fun onToken(token: String?)
}

interface SignInListener {
    fun onSignedIn()

    /** The user closed the provider's sheet. */
    fun onCancelled()

    /** The email already has an account that signs in with [provider]. */
    fun onAccountExistsWithOtherProvider(provider: SignInProvider)

    /** Sign-in failed for any other reason; [message] can be shown to the user. */
    fun onFailed(message: String)
}
