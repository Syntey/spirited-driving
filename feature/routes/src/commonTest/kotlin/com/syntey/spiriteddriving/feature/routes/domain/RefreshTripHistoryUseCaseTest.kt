package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RefreshTripHistoryUseCaseTest {

    @Test
    fun `the first page from the backend replaces the kept drives`() = runTest {
        val cache = FakeTripHistoryCache(page = PastDrivePage(drives = emptyList(), nextCursor = null))
        val source = FakeTripHistorySource(drives = listOf(FakeTripHistorySource.Recorded))

        useCase(source = source, cache = cache).build(Unit)

        assertEquals(PastDrivePage(drives = listOf(FakeTripHistorySource.Recorded), nextCursor = null), cache.get())
    }

    @Test
    fun `a failed refresh keeps the drives on the device`() = runTest {
        val kept = PastDrivePage(drives = listOf(FakeTripHistorySource.Recorded), nextCursor = null)
        val cache = FakeTripHistoryCache(page = kept)
        val source = FakeTripHistorySource(drives = emptyList()).apply { error = OfflineException(null) }

        assertFailsWith<OfflineException> { useCase(source = source, cache = cache).build(Unit) }

        assertEquals(kept, cache.get())
    }

    @Test
    fun `a refresh sends the ratings kept on the device before it loads the list`() = runTest {
        val cache = cacheWithUnsent(FakeTripHistorySource.Recorded.id to rating)
        val rater = FakeDriveRater()

        useCase(cache = cache, rater = rater).build(Unit)

        assertEquals(listOf(FakeTripHistorySource.Recorded.id to rating), rater.rated)
        assertTrue(cache.unsentRatings().isEmpty())
    }

    @Test
    fun `an offline refresh stops sending the ratings and the list call throws offline`() = runTest {
        val cache = cacheWithUnsent(FIRST to rating, SECOND to rating)
        val rater = FakeDriveRater().apply { failWith = OfflineException(cause = null) }
        val source = FakeTripHistorySource(drives = emptyList()).apply { error = OfflineException(cause = null) }

        assertFailsWith<OfflineException> { useCase(source = source, cache = cache, rater = rater).build(Unit) }

        assertEquals(listOf(FIRST to rating), rater.rated)
        assertEquals(setOf(FIRST, SECOND), cache.unsentRatings().keys)
    }

    @Test
    fun `a rating the backend won't take is marked sent`() = runTest {
        val cache = cacheWithUnsent(FIRST to rating)
        val rater = FakeDriveRater().apply { failWith = DriveNotRatableException() }

        useCase(cache = cache, rater = rater).build(Unit)

        assertTrue(cache.unsentRatings().isEmpty())
    }

    @Test
    fun `another failure skips its rating, which stays unsent, and the ratings after it still go`() = runTest {
        val cache = cacheWithUnsent(FIRST to rating, SECOND to rating)
        val rater = FailingForDrive(failingId = FIRST)

        useCase(cache = cache, rater = rater).build(Unit)

        assertEquals(listOf(SECOND), rater.sentIds)
        assertEquals(setOf(FIRST), cache.unsentRatings().keys)
    }

    private fun useCase(
        source: FakeTripHistorySource = FakeTripHistorySource(drives = listOf(FakeTripHistorySource.Recorded)),
        cache: FakeTripHistoryCache = FakeTripHistoryCache(),
        rater: DriveRater = FakeDriveRater(),
    ) = RefreshTripHistoryUseCase(source, cache, rater)

    // The kept drives, each with its rating not sent yet
    private suspend fun cacheWithUnsent(vararg ratings: Pair<String, Rating>): FakeTripHistoryCache {
        val drives = ratings.map { (id, _) -> drive(id) }
        val cache = FakeTripHistoryCache(page = PastDrivePage(drives = drives, nextCursor = null))
        ratings.forEach { (id, rating) -> cache.rate(id, rating) }
        return cache
    }

    private fun drive(id: String): PastDrive = FakeTripHistorySource.Recorded.copy(id = id)

    // Refuses the rating of one drive with a failure that isn't offline or not ratable, and accepts the others
    private class FailingForDrive(private val failingId: String) : DriveRater {
        val sentIds = mutableListOf<String>()

        override suspend fun rate(driveId: String, rating: Rating) {
            if (driveId == failingId) throw IllegalStateException("broken")
            sentIds += driveId
        }
    }

    private companion object {
        val rating = Rating(Thumb.Up)
        const val FIRST = "drive-first"
        const val SECOND = "drive-second"
    }
}
