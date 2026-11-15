package com.syntey.spiriteddriving.feature.profile.domain

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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveAccountUseCaseTest {

    @Test
    fun `a signed-in user is emitted at once`() = runTest {
        val useCase = ObserveAccountUseCase(Auth(ObservingPlatformAuth(initialUser = USER)))

        assertEquals(USER, useCase.build(Unit).first())
    }

    @Test
    fun `signed out is emitted as null`() = runTest {
        val useCase = ObserveAccountUseCase(Auth(ObservingPlatformAuth(initialUser = null)))

        assertNull(useCase.build(Unit).first())
    }

    @Test
    fun `a later sign-out emits null after the user`() = runTest {
        val fake = ObservingPlatformAuth(initialUser = USER)
        val emitted = mutableListOf<AuthUser?>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveAccountUseCase(Auth(fake)).build(Unit).collect { emitted += it }
        }

        fake.emitUser(null)
        runCurrent()
        collecting.cancel()

        assertEquals(listOf(USER, null), emitted)
    }
}

private val USER = AuthUser(id = "uid-1", name = "Jan", email = "rider@example.com", provider = SignInProvider.Google)

private class ObservingPlatformAuth(private val initialUser: AuthUser?) : PlatformAuth {

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
