package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignOutUseCaseTest {

    @Test
    fun `signs out of the platform account`() = runTest {
        val fake = SigningOutPlatformAuth()

        SignOutUseCase(Auth(fake)).build(Unit)

        assertEquals(1, fake.signOutCount)
    }
}

private class SigningOutPlatformAuth : PlatformAuth {

    var signOutCount = 0
        private set

    override fun currentUser(): AuthUser? = null

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        listener.onUser(null)
        return AuthListenerRegistration { }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) = Unit

    override fun signOut() {
        signOutCount++
    }

    override fun idToken(listener: TokenListener) = listener.onToken(null)
}
