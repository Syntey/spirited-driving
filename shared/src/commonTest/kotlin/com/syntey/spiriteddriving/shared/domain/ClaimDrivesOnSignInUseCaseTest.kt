package com.syntey.spiriteddriving.shared.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveClaimer
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ClaimDrivesOnSignInUseCaseTest {

    @Test
    fun `sign in claims only the recorded drive IDs`() = runTest {
        val claimer = FakeDriveClaimer()
        val auth = FakePlatformAuth()
        val useCase = useCase(auth, claimer, drive("a", isRecorded = true), drive("b"), drive("c", isRecorded = true))
        start(useCase)

        auth.becomeSignedIn()
        runCurrent()

        assertEquals(listOf(listOf("a", "c")), claimer.claimed)
    }

    @Test
    fun `nothing is claimed while signed out`() = runTest {
        val claimer = FakeDriveClaimer()
        val useCase = useCase(FakePlatformAuth(), claimer, drive("a", isRecorded = true))
        start(useCase)

        runCurrent()

        assertTrue(claimer.claimed.isEmpty())
    }

    @Test
    fun `nothing is claimed when no drive is recorded`() = runTest {
        val claimer = FakeDriveClaimer()
        val auth = FakePlatformAuth()
        start(useCase(auth, claimer, drive("a"), drive("b")))

        auth.becomeSignedIn()
        runCurrent()

        assertTrue(claimer.claimed.isEmpty())
    }

    @Test
    fun `sign in after sign out claims again`() = runTest {
        val claimer = FakeDriveClaimer()
        val auth = FakePlatformAuth()
        start(useCase(auth, claimer, drive("a", isRecorded = true)))

        auth.becomeSignedIn()
        runCurrent()
        auth.becomeSignedOut()
        runCurrent()
        auth.becomeSignedIn()
        runCurrent()

        assertEquals(listOf(listOf("a"), listOf("a")), claimer.claimed)
    }

    @Test
    fun `a failed claim does not stop the next sign in from claiming`() = runTest {
        val claimer = FakeDriveClaimer().apply { failWith = IllegalStateException("No connection") }
        val auth = FakePlatformAuth()
        start(useCase(auth, claimer, drive("a", isRecorded = true)))

        auth.becomeSignedIn()
        runCurrent()
        auth.becomeSignedOut()
        claimer.failWith = null
        runCurrent()
        auth.becomeSignedIn()
        runCurrent()

        assertEquals(listOf(listOf("a"), listOf("a")), claimer.claimed)
    }

    private fun TestScope.start(useCase: ClaimDrivesOnSignInUseCase) {
        backgroundScope.launch { useCase.build(Unit).collect { } }
    }

    private fun useCase(auth: FakePlatformAuth, claimer: FakeDriveClaimer, vararg drives: Drive) = ClaimDrivesOnSignInUseCase(
        auth = Auth(auth),
        startedDrives = FakeStartedDrives(drives.toList()),
        driveClaimer = claimer,
    )

    private fun drive(id: String, isRecorded: Boolean = false) = Drive(
        id = id,
        routeId = "route-1",
        startedAt = Instant.parse("2026-11-13T10:15:30Z"),
        plannedDuration = 3900.seconds,
        isRecorded = isRecorded,
    )
}

private class FakePlatformAuth : PlatformAuth {

    private var user: AuthUser? = null
    private var listener: AuthUserListener? = null

    override fun currentUser(): AuthUser? = user

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        this.listener = listener
        listener.onUser(user)
        return AuthListenerRegistration { this.listener = null }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) = Unit

    override fun signOut() = Unit

    override fun idToken(listener: TokenListener) = listener.onToken(null)

    fun becomeSignedIn() = emit(USER)

    fun becomeSignedOut() = emit(null)

    private fun emit(newUser: AuthUser?) {
        user = newUser
        listener?.onUser(newUser)
    }
}

private val USER = AuthUser(id = "uid-1", name = "Jan", email = null, provider = SignInProvider.Google)
