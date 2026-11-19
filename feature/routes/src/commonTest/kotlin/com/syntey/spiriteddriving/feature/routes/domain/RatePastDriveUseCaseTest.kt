package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RatePastDriveUseCaseTest {

    private val drive = FakeTripHistorySource.Recorded
    private val rating = Rating(Thumb.Up)
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `a rating sent online is kept on the drive and not left to send`() = runTest {
        val cache = cacheWithDrive()
        val rater = FakeDriveRater()

        useCase(cache = cache, rater = rater).build(RatePastDriveUseCase.Args(drive.id, rating))

        assertEquals(listOf(drive.id to rating), rater.rated)
        assertEquals(rating, cache.get()?.drives?.single()?.rating)
        assertTrue(cache.unsentRatings().isEmpty())
    }

    @Test
    fun `a rating without a connection is kept unsent and doesn't throw`() = runTest {
        val cache = cacheWithDrive()
        val rater = FakeDriveRater().apply { failWith = OfflineException(cause = null) }

        useCase(cache = cache, rater = rater).build(RatePastDriveUseCase.Args(drive.id, rating))

        assertEquals(rating, cache.get()?.drives?.single()?.rating)
        assertEquals(mapOf(drive.id to rating), cache.unsentRatings())
    }

    @Test
    fun `a rating the backend won't take is marked sent so it isn't retried`() = runTest {
        val cache = cacheWithDrive()
        val rater = FakeDriveRater().apply { failWith = DriveNotRatableException() }

        useCase(cache = cache, rater = rater).build(RatePastDriveUseCase.Args(drive.id, rating))

        assertEquals(rating, cache.get()?.drives?.single()?.rating)
        assertTrue(cache.unsentRatings().isEmpty())
    }

    @Test
    fun `another failure propagates and keeps the rating unsent`() = runTest {
        val cache = cacheWithDrive()
        val rater = FakeDriveRater().apply { failWith = IllegalStateException("broken") }

        assertFailsWith<IllegalStateException> {
            useCase(cache = cache, rater = rater).build(RatePastDriveUseCase.Args(drive.id, rating))
        }

        assertEquals(mapOf(drive.id to rating), cache.unsentRatings())
    }

    @Test
    fun `a started drive on this phone gets the rating too, once it is sent`() = runTest {
        val started = startedDrive()
        val startedDrives = FakeStartedDrives(listOf(started))

        useCase(startedDrives = startedDrives, cache = cacheWithDrive()).build(RatePastDriveUseCase.Args(drive.id, rating))

        assertEquals(rating, startedDrives.drives.single().rating)
        assertTrue(startedDrives.drives.single().isRatingSent)
    }

    @Test
    fun `a started drive keeps its rating as not sent while offline`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(startedDrive()))
        val rater = FakeDriveRater().apply { failWith = OfflineException(cause = null) }

        useCase(startedDrives = startedDrives, cache = cacheWithDrive(), rater = rater)
            .build(RatePastDriveUseCase.Args(drive.id, rating))

        assertEquals(rating, startedDrives.drives.single().rating)
        assertFalse(startedDrives.drives.single().isRatingSent)
    }

    @Test
    fun `rating a drive ends its drive feedback question and cancels its notification`() = runTest {
        val questions = FakeOpenDriveQuestion(question(drive.id))
        val reminders = FakePlatformDriveReminders()

        useCase(questions = questions, reminders = reminders, cache = cacheWithDrive())
            .build(RatePastDriveUseCase.Args(drive.id, rating))

        assertNull(questions.question)
        assertEquals(listOf(drive.id), reminders.cancelled)
    }

    private fun useCase(
        questions: FakeOpenDriveQuestion = FakeOpenDriveQuestion(),
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
        cache: FakeTripHistoryCache = cacheWithDrive(),
        startedDrives: FakeStartedDrives = FakeStartedDrives(),
        rater: FakeDriveRater = FakeDriveRater(),
    ) = RatePastDriveUseCase(questions, DriveReminders(reminders), cache, startedDrives, rater)

    private fun cacheWithDrive() = FakeTripHistoryCache(page = PastDrivePage(drives = listOf(drive), nextCursor = null))

    private fun startedDrive() = Drive(
        id = drive.id,
        routeId = drive.routeId,
        startedAt = FakeTripHistorySource.STARTED_AT,
        plannedDuration = drive.duration,
        isRecorded = true,
    )

    private fun question(driveId: String) = DriveQuestion(
        driveId = driveId,
        startedAt = FakeTripHistorySource.STARTED_AT,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )
}
