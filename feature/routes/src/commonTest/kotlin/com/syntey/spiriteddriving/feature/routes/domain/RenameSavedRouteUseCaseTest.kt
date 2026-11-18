package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RenameSavedRouteUseCaseTest {

    @Test
    fun `the new name is trimmed and kept on the device`() = runTest {
        val source = FakeSavedRoutesSource(routes = listOf(FakeSavedRoutesSource.Recorded))
        val cache = FakeSavedRoutesCache(routes = listOf(FakeSavedRoutesSource.Recorded))

        RenameSavedRouteUseCase(source, cache).build(RenameSavedRouteUseCase.Args(id = "saved-1", name = "  Sunday loop "))

        assertEquals(listOf("Sunday loop"), cache.get()?.map { it.name })
        assertEquals(listOf("Sunday loop"), source.list().map { it.name })
    }

    @Test
    fun `a failed rename keeps the name on the device`() = runTest {
        val source = FakeSavedRoutesSource(routes = listOf(FakeSavedRoutesSource.Recorded)).apply { error = OfflineException(null) }
        val cache = FakeSavedRoutesCache(routes = listOf(FakeSavedRoutesSource.Recorded))

        assertFailsWith<OfflineException> {
            RenameSavedRouteUseCase(source, cache).build(RenameSavedRouteUseCase.Args(id = "saved-1", name = "Sunday loop"))
        }

        assertEquals(listOf("Karlštejn Castle"), cache.get()?.map { it.name })
    }
}
