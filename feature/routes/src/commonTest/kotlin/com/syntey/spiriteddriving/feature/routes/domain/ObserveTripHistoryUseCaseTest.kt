package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTripHistoryUseCaseTest {

    @Test
    fun `the kept drives are emitted as they are`() = runTest {
        val page = PastDrivePage(drives = listOf(FakeTripHistorySource.Recorded), nextCursor = "1")
        val useCase = ObserveTripHistoryUseCase(FakeTripHistoryCache(page))

        assertEquals(page, useCase.build(Unit).first())
    }

    @Test
    fun `nothing kept is emitted as null`() = runTest {
        val useCase = ObserveTripHistoryUseCase(FakeTripHistoryCache(page = null))

        assertEquals(null, useCase.build(Unit).first())
    }
}
