package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSavedRoutesUseCaseTest {

    @Test
    fun `the kept routes come newest first`() = runTest {
        val older = savedRoute(id = "older", savedAt = SAVED_AT)
        val newer = savedRoute(id = "newer", savedAt = SAVED_AT + 2.days)
        val useCase = ObserveSavedRoutesUseCase(FakeSavedRoutesCache(routes = listOf(older, newer)))

        assertEquals(listOf(newer, older), useCase.build(Unit).first())
    }

    @Test
    fun `nothing kept stays null`() = runTest {
        val useCase = ObserveSavedRoutesUseCase(FakeSavedRoutesCache(routes = null))

        assertEquals(null, useCase.build(Unit).first())
    }

    private fun savedRoute(id: String, savedAt: Instant): SavedRoute = FakeSavedRoutesSource.Recorded.copy(
        id = SavedRouteId(id),
        savedAt = savedAt,
    )

    private companion object {
        val SAVED_AT: Instant = Instant.parse("2026-11-08T09:30:00Z")
    }
}
