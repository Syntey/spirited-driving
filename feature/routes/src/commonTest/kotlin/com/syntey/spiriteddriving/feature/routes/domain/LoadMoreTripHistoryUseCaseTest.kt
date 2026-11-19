package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoadMoreTripHistoryUseCaseTest {

    @Test
    fun `the page after the kept drives is added at the end`() = runTest {
        val first = FakeTripHistorySource.Recorded
        val second = first.copy(id = "drive-2")
        val cache = FakeTripHistoryCache(page = PastDrivePage(drives = listOf(first), nextCursor = "1"))
        val source = FakeTripHistorySource(drives = listOf(first, second))

        LoadMoreTripHistoryUseCase(source, cache).build(Unit)

        assertEquals(PastDrivePage(drives = listOf(first, second), nextCursor = null), cache.get())
    }

    @Test
    fun `nothing is fetched when the last page is kept`() = runTest {
        val kept = PastDrivePage(drives = listOf(FakeTripHistorySource.Recorded), nextCursor = null)
        val cache = FakeTripHistoryCache(page = kept)
        val source = FakeTripHistorySource(drives = emptyList()).apply { error = OfflineException(null) }

        LoadMoreTripHistoryUseCase(source, cache).build(Unit)

        assertEquals(kept, cache.get())
    }

    @Test
    fun `a failed load keeps the drives already kept`() = runTest {
        val kept = PastDrivePage(drives = listOf(FakeTripHistorySource.Recorded), nextCursor = "1")
        val cache = FakeTripHistoryCache(page = kept)
        val source = FakeTripHistorySource(
            drives = listOf(FakeTripHistorySource.Recorded, FakeTripHistorySource.Recorded.copy(id = "drive-2")),
        )
            .apply { error = OfflineException(null) }

        assertFailsWith<OfflineException> { LoadMoreTripHistoryUseCase(source, cache).build(Unit) }

        assertEquals(kept, cache.get())
    }
}
