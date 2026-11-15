package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthTest {

    @Test
    fun `signed in listener maps to signed in result`() = runTest {
        val fake = FakePlatformAuth(signInOutcome = { it.onSignedIn() })

        assertEquals(SignInResult.SignedIn, Auth(fake).signIn(SignInProvider.Google))
    }

    @Test
    fun `cancelled listener maps to cancelled result`() = runTest {
        val fake = FakePlatformAuth(signInOutcome = { it.onCancelled() })

        assertEquals(SignInResult.Cancelled, Auth(fake).signIn(SignInProvider.Apple))
    }

    @Test
    fun `other provider listener maps to account exists result`() = runTest {
        val fake = FakePlatformAuth(signInOutcome = { it.onAccountExistsWithOtherProvider(SignInProvider.Apple) })

        assertEquals(
            SignInResult.AccountExistsWithOtherProvider(SignInProvider.Apple),
            Auth(fake).signIn(SignInProvider.Google),
        )
    }

    @Test
    fun `failed listener maps to failed result with its message`() = runTest {
        val fake = FakePlatformAuth(signInOutcome = { it.onFailed("No network") })

        assertEquals(SignInResult.Failed("No network"), Auth(fake).signIn(SignInProvider.Google))
    }

    @Test
    fun `user flow emits the listener's users`() = runTest {
        val fake = FakePlatformAuth()
        val received = mutableListOf<AuthUser?>()
        val job = launch { Auth(fake).user.collect { received += it } }
        runCurrent()

        fake.emitUser(USER)
        runCurrent()
        fake.emitUser(null)
        runCurrent()

        assertEquals(listOf(null, USER, null), received)
        job.cancelAndJoin()
    }

    @Test
    fun `cancelling the user flow removes the listener`() = runTest {
        val fake = FakePlatformAuth()
        val job = launch { Auth(fake).user.collect { } }
        runCurrent()

        job.cancelAndJoin()

        assertTrue(fake.registrationRemoved)
    }

    @Test
    fun `id token returns the listener's token`() = runTest {
        val fake = FakePlatformAuth(token = "id-token")

        assertEquals("id-token", Auth(fake).idToken())
    }

    @Test
    fun `id token is null when the listener has none`() = runTest {
        val fake = FakePlatformAuth(token = null)

        assertNull(Auth(fake).idToken())
    }
}

private val USER = AuthUser(id = "uid-1", name = "Jan", email = "jan@example.com", provider = SignInProvider.Google)

private class FakePlatformAuth(private val signInOutcome: (SignInListener) -> Unit = {}, private val token: String? = null) :
    PlatformAuth {

    private var userListener: AuthUserListener? = null
    var registrationRemoved = false
        private set

    override fun currentUser(): AuthUser? = null

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        userListener = listener
        listener.onUser(null)
        return AuthListenerRegistration {
            registrationRemoved = true
            userListener = null
        }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) = signInOutcome(listener)

    override fun signOut() = Unit

    override fun idToken(listener: TokenListener) = listener.onToken(token)

    fun emitUser(user: AuthUser?) {
        checkNotNull(userListener).onUser(user)
    }
}
