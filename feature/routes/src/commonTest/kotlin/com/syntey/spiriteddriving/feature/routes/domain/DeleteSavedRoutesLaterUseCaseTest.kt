package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.work.FakeAppWork
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteSavedRoutesLaterUseCaseTest {

    private val routes = listOf(route("a"), route("b"), route("c"))

    @Test
    fun `each route is deleted on the backend and removed from the device in app work`() = runTest {
        val source = FakeSavedRoutesSource(routes = routes)
        val cache = FakeSavedRoutesCache(routes = routes)
        val appWork = FakeAppWork()

        DeleteSavedRoutesLaterUseCase(appWork, DeleteSavedRouteUseCase(source, cache))(setOf("a", "b"))
        appWork.runAll()

        assertEquals(listOf("c"), source.routes.map { it.id })
        assertEquals(listOf("c"), cache.get()?.map { it.id })
    }

    @Test
    fun `nothing is deleted before the app work runs`() = runTest {
        val source = FakeSavedRoutesSource(routes = routes)
        val appWork = FakeAppWork()

        DeleteSavedRoutesLaterUseCase(appWork, DeleteSavedRouteUseCase(source, FakeSavedRoutesCache(routes = routes)))(setOf("a"))

        assertEquals(routes, source.routes)
    }

    @Test
    fun `a delete that fails keeps the route on the device`() = runTest {
        val source = FakeSavedRoutesSource(routes = routes).apply { error = OfflineException(null) }
        val cache = FakeSavedRoutesCache(routes = routes)
        val appWork = FakeAppWork()

        DeleteSavedRoutesLaterUseCase(appWork, DeleteSavedRouteUseCase(source, cache))(setOf("a"))
        appWork.runAll()

        assertEquals(routes, cache.get())
    }

    private fun route(id: String): SavedRoute = FakeSavedRoutesSource.Recorded.copy(id = id)
}
