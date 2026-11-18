package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSignedInUseCaseTest {

    @Test
    fun `a signed-in user is emitted as signed in at once`() = runTest {
        val useCase = ObserveSignedInUseCase(Auth(FakePlatformAuth(initialUser = USER)))

        assertTrue(useCase.build(Unit).first())
    }

    @Test
    fun `signed out is emitted as not signed in`() = runTest {
        val useCase = ObserveSignedInUseCase(Auth(FakePlatformAuth(initialUser = null)))

        assertFalse(useCase.build(Unit).first())
    }

    @Test
    fun `a switch between users is not emitted again`() = runTest {
        val fake = FakePlatformAuth(initialUser = USER)
        val emitted = mutableListOf<Boolean>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveSignedInUseCase(Auth(fake)).build(Unit).collect { emitted += it }
        }

        fake.emitUser(OTHER_USER)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(true), emitted)
    }

    @Test
    fun `a sign-out after a sign-in emits not signed in`() = runTest {
        val fake = FakePlatformAuth(initialUser = null)
        val emitted = mutableListOf<Boolean>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveSignedInUseCase(Auth(fake)).build(Unit).collect { emitted += it }
        }

        fake.emitUser(USER)
        runCurrent()
        fake.emitUser(null)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(false, true, false), emitted)
    }
}

private val USER = AuthUser(id = "uid-1", name = "Jan", email = null, provider = SignInProvider.Google)
private val OTHER_USER = AuthUser(id = "uid-2", name = "Eva", email = null, provider = SignInProvider.Google)

private class FakePlatformAuth(private val initialUser: AuthUser?) : PlatformAuth {

    private var userListener: AuthUserListener? = null

    override fun currentUser(): AuthUser? = initialUser

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        userListener = listener
        listener.onUser(initialUser)
        return AuthListenerRegistration { userListener = null }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) = Unit

    override fun signOut() = Unit

    override fun idToken(listener: TokenListener) = listener.onToken(null)

    fun emitUser(user: AuthUser?) {
        checkNotNull(userListener).onUser(user)
    }
}
