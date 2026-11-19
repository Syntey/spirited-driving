package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RefreshSavedRoutesUseCaseTest {

    @Test
    fun `the routes from the backend replace the kept ones`() = runTest {
        val cache = FakeSavedRoutesCache(routes = emptyList())
        val useCase = RefreshSavedRoutesUseCase(FakeSavedRoutesSource(routes = listOf(FakeSavedRoutesSource.Recorded)), cache)

        useCase.build(Unit)

        assertEquals(listOf(FakeSavedRoutesSource.Recorded), cache.get())
    }

    @Test
    fun `a failed load keeps the routes on the device`() = runTest {
        val cache = FakeSavedRoutesCache(routes = listOf(FakeSavedRoutesSource.Recorded))
        val source = FakeSavedRoutesSource(routes = emptyList()).apply { error = OfflineException(null) }

        assertFailsWith<OfflineException> { RefreshSavedRoutesUseCase(source, cache).build(Unit) }

        assertEquals(listOf(FakeSavedRoutesSource.Recorded), cache.get())
    }
}
