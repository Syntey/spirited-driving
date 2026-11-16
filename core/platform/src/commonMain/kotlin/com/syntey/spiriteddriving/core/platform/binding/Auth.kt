package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Coroutine and Flow view of [PlatformAuth], for use cases and `:data:*` classes. */
class Auth(private val binding: PlatformAuth) {

    /** The signed-in user, emitted at once and on each change. */
    val user: Flow<AuthUser?> = callbackFlow {
        val registration = binding.addUserListener { trySend(it) }
        awaitClose { registration.remove() }
    }.distinctUntilChanged()

    fun currentUser(): AuthUser? = binding.currentUser()

    /** Signs in with [provider] and returns how it ended. */
    suspend fun signIn(provider: SignInProvider): SignInResult = suspendCancellableCoroutine { continuation ->
        binding.signIn(
            provider,
            object : SignInListener {
                override fun onSignedIn() = resumeOnce(SignInResult.SignedIn)
                override fun onCancelled() = resumeOnce(SignInResult.Cancelled)
                override fun onAccountExistsWithOtherProvider(provider: SignInProvider) =
                    resumeOnce(SignInResult.AccountExistsWithOtherProvider(provider))
                override fun onFailed(message: String) = resumeOnce(SignInResult.Failed(message))

                private fun resumeOnce(result: SignInResult) {
                    if (continuation.isActive) continuation.resume(result)
                }
            },
        )
    }

    /** The Firebase ID token, or null when signed out or none can be had. */
    suspend fun idToken(): String? = suspendCancellableCoroutine { continuation ->
        binding.idToken { token ->
            if (continuation.isActive) continuation.resume(token)
        }
    }

    fun signOut() = binding.signOut()
}
