package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.SignInResult
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInUseCaseTest {

    @Test
    fun `signs in with the provider it is given`() = runTest {
        val fake = SigningInPlatformAuth(outcome = { it.onSignedIn() })

        SignInUseCase(Auth(fake)).build(SignInProvider.Google)

        assertEquals(listOf(SignInProvider.Google), fake.requestedProviders)
    }

    @Test
    fun `returns how the sign-in ended`() = runTest {
        val fake = SigningInPlatformAuth(outcome = { it.onAccountExistsWithOtherProvider(SignInProvider.Apple) })

        assertEquals(
            SignInResult.AccountExistsWithOtherProvider(SignInProvider.Apple),
            SignInUseCase(Auth(fake)).build(SignInProvider.Google),
        )
    }

    @Test
    fun `a cancelled sign-in returns cancelled`() = runTest {
        val fake = SigningInPlatformAuth(outcome = { it.onCancelled() })

        assertEquals(SignInResult.Cancelled, SignInUseCase(Auth(fake)).build(SignInProvider.Google))
    }
}

private class SigningInPlatformAuth(private val outcome: (SignInListener) -> Unit) : PlatformAuth {

    val requestedProviders = mutableListOf<SignInProvider>()

    override fun currentUser(): AuthUser? = null

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        listener.onUser(null)
        return AuthListenerRegistration { }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) {
        requestedProviders += provider
        outcome(listener)
    }

    override fun signOut() = Unit

    override fun idToken(listener: TokenListener) = listener.onToken(null)
}
