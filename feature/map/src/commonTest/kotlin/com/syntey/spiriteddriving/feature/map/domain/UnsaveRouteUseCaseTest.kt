package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.toSavedRoute
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class UnsaveRouteUseCaseTest {

    private val saved = FakeRoutePlanner.Recorded.funRoutes.first()
        .toSavedRoute(id = "saved-1", name = "Karlštejn Castle", savedAt = Instant.parse("2026-11-08T09:30:00Z"))

    @Test
    fun `the saved route is deleted from the backend`() = runTest {
        val source = FakeSavedRoutesSource(routes = listOf(saved))

        UnsaveRouteUseCase(source, FakeSavedRoutesCache(routes = listOf(saved))).build(saved.id)

        assertEquals(listOf(saved.id), source.deleted)
    }

    @Test
    fun `the saved route is removed from the device`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(saved))
        cache.putRoute(saved.id, FakeRoutePlanner.Recorded.funRoutes.first())

        UnsaveRouteUseCase(FakeSavedRoutesSource(routes = listOf(saved)), cache).build(saved.id)

        assertEquals(emptyList(), cache.get())
        assertNull(cache.getRoute(saved.id))
    }
}
