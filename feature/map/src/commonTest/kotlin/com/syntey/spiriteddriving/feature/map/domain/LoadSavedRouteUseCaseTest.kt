package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoadSavedRouteUseCaseTest {

    private val saved = FakeSavedRoutesSource.Recorded
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `a saved route's route kept on the device is used without asking the backend`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(saved))
        cache.putRoute(saved.id, route)
        // Empty, so asking it for the saved route would throw
        val source = FakeSavedRoutesSource(routes = emptyList())

        assertEquals(route, LoadSavedRouteUseCase(source, cache).build(saved.id))
    }

    @Test
    fun `a saved route's route not kept on the device comes from the backend`() = runTest {
        val source = FakeSavedRoutesSource(routes = listOf(saved))

        val loaded = LoadSavedRouteUseCase(source, FakeSavedRoutesCache(routes = listOf(saved))).build(saved.id)

        assertEquals(saved.routeId, loaded.id)
    }

    @Test
    fun `a route loaded from the backend is kept on the device`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(saved))

        LoadSavedRouteUseCase(FakeSavedRoutesSource(routes = listOf(saved)), cache).build(saved.id)

        assertNotNull(cache.getRoute(saved.id))
    }
}
