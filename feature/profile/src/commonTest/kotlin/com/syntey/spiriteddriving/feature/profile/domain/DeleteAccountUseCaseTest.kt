package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.domain.account.FakeAccountDeleter
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
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DeleteAccountUseCaseTest {

    @Test
    fun `deletes the account once and signs out`() = runTest {
        val deleter = FakeAccountDeleter()
        val auth = DeletingPlatformAuth()

        DeleteAccountUseCase(deleter, Auth(auth)).build(Unit)

        assertEquals(1, deleter.deleteCount)
        assertEquals(1, auth.signOutCount)
    }

    @Test
    fun `a failed deletion is thrown and does not sign out`() = runTest {
        val failure = IllegalStateException("Backend failed")
        val deleter = FakeAccountDeleter().apply { failWith = failure }
        val auth = DeletingPlatformAuth()

        val thrown = assertFailsWith<IllegalStateException> {
            DeleteAccountUseCase(deleter, Auth(auth)).build(Unit)
        }

        assertSame(failure, thrown)
        assertEquals(0, auth.signOutCount)
    }
}

private class DeletingPlatformAuth : PlatformAuth {

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
