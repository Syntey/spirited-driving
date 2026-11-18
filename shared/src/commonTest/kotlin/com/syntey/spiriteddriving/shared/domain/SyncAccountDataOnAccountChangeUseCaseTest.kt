package com.syntey.spiriteddriving.shared.domain

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyncAccountDataOnAccountChangeUseCaseTest {

    private val kept = FakeSavedRoutesSource.Recorded
    private val onBackend = FakeSavedRoutesSource.Recorded.copy(id = "saved-2", name = "Kokořín")
    private val keptTrips = PastDrivePage(listOf(FakeTripHistorySource.Recorded), nextCursor = null)

    @Test
    fun `signed in at start replaces the kept routes with the backend's`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(kept))
        start(useCase(SwitchableAuth(user = USER), cache, FakeSavedRoutesSource(listOf(onBackend))))

        runCurrent()

        assertEquals(listOf(onBackend), cache.get())
    }

    @Test
    fun `signed in at start and offline keeps the kept routes`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(kept))
        val source = FakeSavedRoutesSource().apply { error = OfflineException(null) }
        start(useCase(SwitchableAuth(user = USER), cache, source))

        runCurrent()

        assertEquals(listOf(kept), cache.get())
    }

    @Test
    fun `signed in at start keeps the kept trip history`() = runTest {
        val trips = FakeTripHistoryCache(page = keptTrips)
        start(useCase(SwitchableAuth(user = USER), FakeSavedRoutesCache(), FakeSavedRoutesSource(), trips))

        runCurrent()

        assertEquals(keptTrips, trips.get())
    }

    @Test
    fun `sign out clears the saved routes`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(kept))
        val auth = SwitchableAuth(user = USER)
        start(useCase(auth, cache, FakeSavedRoutesSource(listOf(kept))))
        runCurrent()

        auth.emit(null)
        runCurrent()

        assertNull(cache.get())
    }

    @Test
    fun `sign out clears the trip history`() = runTest {
        val trips = FakeTripHistoryCache(page = keptTrips)
        val auth = SwitchableAuth(user = USER)
        start(useCase(auth, FakeSavedRoutesCache(), FakeSavedRoutesSource(), trips))
        runCurrent()

        auth.emit(null)
        runCurrent()

        assertNull(trips.get())
    }

    @Test
    fun `switching account offline leaves nothing of the previous account`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(kept))
        val auth = SwitchableAuth(user = USER)
        val source = FakeSavedRoutesSource(listOf(kept))
        start(useCase(auth, cache, source))
        runCurrent()
        source.error = OfflineException(null)

        auth.emit(OTHER_USER)
        runCurrent()

        assertNull(cache.get())
    }

    @Test
    fun `switching account clears the trip history`() = runTest {
        val trips = FakeTripHistoryCache(page = keptTrips)
        val auth = SwitchableAuth(user = USER)
        start(useCase(auth, FakeSavedRoutesCache(), FakeSavedRoutesSource(), trips))
        runCurrent()

        auth.emit(OTHER_USER)
        runCurrent()

        assertNull(trips.get())
    }

    @Test
    fun `signed out at start clears leftover saved routes`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(kept))
        start(useCase(SwitchableAuth(user = null), cache, FakeSavedRoutesSource()))

        runCurrent()

        assertNull(cache.get())
    }

    @Test
    fun `signed out at start clears leftover trip history`() = runTest {
        val trips = FakeTripHistoryCache(page = keptTrips)
        start(useCase(SwitchableAuth(user = null), FakeSavedRoutesCache(), FakeSavedRoutesSource(), trips))

        runCurrent()

        assertNull(trips.get())
    }

    @Test
    fun `signing in loads the account's saved routes`() = runTest {
        val cache = FakeSavedRoutesCache()
        val auth = SwitchableAuth(user = null)
        start(useCase(auth, cache, FakeSavedRoutesSource(listOf(onBackend))))
        runCurrent()

        auth.emit(USER)
        runCurrent()

        assertEquals(listOf(onBackend), cache.get())
    }

    @Test
    fun `a clear that failed on sign out is made up on the next sign in`() = runTest {
        val cache = FailingOnceSavedRoutesCache(FakeSavedRoutesCache(routes = listOf(kept)))
        val auth = SwitchableAuth(user = USER)
        val source = FakeSavedRoutesSource(listOf(kept)).apply { error = OfflineException(null) }
        start(useCase(auth, cache, source))
        runCurrent()
        auth.emit(null)
        runCurrent()

        auth.emit(OTHER_USER)
        runCurrent()

        assertNull(cache.get())
    }

    @Test
    fun `a failing saved routes clear does not skip the trip history`() = runTest {
        val trips = FakeTripHistoryCache(page = keptTrips)
        val cache = FailingOnceSavedRoutesCache(FakeSavedRoutesCache(routes = listOf(kept)))
        val auth = SwitchableAuth(user = USER)
        start(useCase(auth, cache, FakeSavedRoutesSource(), trips))
        runCurrent()

        auth.emit(null)
        runCurrent()

        assertNull(trips.get())
    }

    private fun TestScope.start(useCase: SyncAccountDataOnAccountChangeUseCase) {
        backgroundScope.launch { useCase.build(Unit).collect { } }
    }

    private fun useCase(
        auth: SwitchableAuth,
        cache: SavedRoutesCache,
        source: FakeSavedRoutesSource,
        tripHistoryCache: TripHistoryCache = FakeTripHistoryCache(),
    ) = SyncAccountDataOnAccountChangeUseCase(
        auth = Auth(auth),
        savedRoutesSource = source,
        savedRoutesCache = cache,
        tripHistoryCache = tripHistoryCache,
    )
}

private class FailingOnceSavedRoutesCache(private val delegate: SavedRoutesCache) : SavedRoutesCache by delegate {

    private var failNextClear = true

    override suspend fun clear() {
        if (failNextClear) {
            failNextClear = false
            error("Disk unavailable")
        }
        delegate.clear()
    }
}

private class SwitchableAuth(user: AuthUser?) : PlatformAuth {

    private var user: AuthUser? = user
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

    fun emit(newUser: AuthUser?) {
        user = newUser
        listener?.onUser(newUser)
    }
}

private val USER = AuthUser(id = "uid-1", name = "Jan", email = null, provider = SignInProvider.Google)
private val OTHER_USER = AuthUser(id = "uid-2", name = "Eva", email = null, provider = SignInProvider.Google)
