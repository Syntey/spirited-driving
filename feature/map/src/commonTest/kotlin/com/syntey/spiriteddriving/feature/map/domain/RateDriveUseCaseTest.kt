package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class RateDriveUseCaseTest {

    private val at = Instant.parse("2026-11-13T10:00:00Z")
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()
    private val rating = Rating(Thumb.Up, setOf(RatingReason.CurvyRoads))

    @Test
    fun `rating clears the question and keeps the rating`() = runTest {
        val questions = FakeOpenDriveQuestion(question("drive-1"))
        val startedDrives = FakeStartedDrives(listOf(drive(recorded = true)))

        useCase(startedDrives, questions = questions).build(RateDriveUseCase.Args("drive-1", rating))

        assertNull(questions.question)
        assertEquals(rating, startedDrives.all().single().rating)
    }

    @Test
    fun `rating cancels the drive's notification`() = runTest {
        val reminders = FakePlatformDriveReminders()

        useCase(FakeStartedDrives(listOf(drive(recorded = true))), reminders = reminders)
            .build(RateDriveUseCase.Args("drive-1", rating))

        assertEquals(listOf("drive-1"), reminders.cancelled)
    }

    @Test
    fun `rating sends the rating and marks it sent`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive(recorded = true)))
        val rater = FakeDriveRater()

        useCase(startedDrives, rater = rater).build(RateDriveUseCase.Args("drive-1", rating))

        assertEquals(listOf("drive-1" to rating), rater.rated)
        assertTrue(startedDrives.all().single().isRatingSent)
    }

    @Test
    fun `without a connection the rating stays unsent and the error is thrown`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive(recorded = true)))
        val rater = FakeDriveRater().apply { failWith = OfflineException(cause = null) }

        assertFailsWith<OfflineException> {
            useCase(startedDrives, rater = rater).build(RateDriveUseCase.Args("drive-1", rating))
        }

        val stored = startedDrives.all().single()
        assertEquals(rating, stored.rating)
        assertFalse(stored.isRatingSent)
    }

    @Test
    fun `a drive that is not recorded yet is recorded before its rating is sent`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive(recorded = false)))
        val recorder = FakeDriveRecorder()
        val rater = FakeDriveRater()

        useCase(startedDrives, recorder = recorder, rater = rater).build(RateDriveUseCase.Args("drive-1", rating))

        assertEquals(listOf("drive-1"), recorder.recorded.map { it.id })
        assertEquals(listOf("drive-1" to rating), rater.rated)
        val stored = startedDrives.all().single()
        assertTrue(stored.isRecorded)
        assertTrue(stored.isRatingSent)
    }

    private fun useCase(
        startedDrives: FakeStartedDrives,
        recorder: FakeDriveRecorder = FakeDriveRecorder(),
        rater: FakeDriveRater = FakeDriveRater(),
        questions: FakeOpenDriveQuestion = FakeOpenDriveQuestion(),
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
    ) = RateDriveUseCase(questions, startedDrives, DriveSender(startedDrives, recorder, rater), DriveReminders(reminders))

    private fun drive(recorded: Boolean) = Drive(
        id = "drive-1",
        routeId = route.id,
        startedAt = at,
        plannedDuration = route.duration,
        isRecorded = recorded,
    )

    private fun question(driveId: String) = DriveQuestion(
        driveId = driveId,
        startedAt = at,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )
}
