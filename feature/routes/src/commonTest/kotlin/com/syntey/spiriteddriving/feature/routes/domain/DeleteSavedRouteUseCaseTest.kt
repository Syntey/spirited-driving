package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeleteSavedRouteUseCaseTest {

    @Test
    fun `the route is deleted on the backend and removed from the device`() = runTest {
        val source = FakeSavedRoutesSource(routes = listOf(FakeSavedRoutesSource.Recorded))
        val cache = FakeSavedRoutesCache(routes = listOf(FakeSavedRoutesSource.Recorded))

        DeleteSavedRouteUseCase(source, cache).build("saved-1")

        assertEquals(emptyList(), source.list())
        assertEquals(emptyList(), cache.get())
    }

    @Test
    fun `a failed delete keeps the route on the device`() = runTest {
        val source = FakeSavedRoutesSource(routes = listOf(FakeSavedRoutesSource.Recorded)).apply { error = OfflineException(null) }
        val cache = FakeSavedRoutesCache(routes = listOf(FakeSavedRoutesSource.Recorded))

        assertFailsWith<OfflineException> { DeleteSavedRouteUseCase(source, cache).build("saved-1") }

        assertEquals(listOf(FakeSavedRoutesSource.Recorded), cache.get())
    }
}
