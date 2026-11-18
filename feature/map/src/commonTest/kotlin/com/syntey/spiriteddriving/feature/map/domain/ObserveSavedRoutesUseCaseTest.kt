package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSavedRoutesUseCaseTest {

    @Test
    fun `the kept saved routes are emitted`() = runTest {
        val useCase = ObserveSavedRoutesUseCase(FakeSavedRoutesCache(routes = listOf(FakeSavedRoutesSource.Recorded)))

        assertEquals(listOf(FakeSavedRoutesSource.Recorded), useCase.build(Unit).first())
    }

    @Test
    fun `nothing kept is emitted as an empty list`() = runTest {
        val useCase = ObserveSavedRoutesUseCase(FakeSavedRoutesCache(routes = null))

        assertEquals(emptyList(), useCase.build(Unit).first())
    }
}
