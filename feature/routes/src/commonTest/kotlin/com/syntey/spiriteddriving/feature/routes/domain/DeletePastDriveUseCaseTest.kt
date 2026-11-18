package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeletePastDriveUseCaseTest {

    private val drive = FakeTripHistorySource.Recorded
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `a drive is removed from the backend and from the device`() = runTest {
        val recorder = FakeDriveRecorder()
        val cache = FakeTripHistoryCache(page = PastDrivePage(drives = listOf(drive), nextCursor = null))

        useCase(recorder = recorder, cache = cache).build(drive.id)

        assertEquals(listOf(drive.id), recorder.removed)
        assertEquals(PastDrivePage(drives = emptyList(), nextCursor = null), cache.get())
    }

    @Test
    fun `deleting the drive clears the question about it and cancels its notification`() = runTest {
        val questions = FakeOpenDriveQuestion(question(drive.id))
        val reminders = FakePlatformDriveReminders()

        useCase(questions = questions, reminders = reminders).build(drive.id)

        assertNull(questions.question)
        assertEquals(listOf(drive.id), reminders.cancelled)
    }

    @Test
    fun `deleting a drive keeps the question about another drive`() = runTest {
        val questions = FakeOpenDriveQuestion(question("other-drive"))

        useCase(questions = questions).build(drive.id)

        assertEquals("other-drive", questions.question?.driveId)
    }

    @Test
    fun `without a connection the drive stays on the device with its question and notification`() = runTest {
        val cache = FakeTripHistoryCache(page = PastDrivePage(drives = listOf(drive), nextCursor = null))
        val questions = FakeOpenDriveQuestion(question(drive.id))
        val reminders = FakePlatformDriveReminders()

        assertFailsWith<OfflineException> {
            useCase(recorder = FailingRemovalRecorder(), cache = cache, questions = questions, reminders = reminders)
                .build(drive.id)
        }

        assertEquals(listOf(drive), cache.get()?.drives)
        assertEquals(drive.id, questions.question?.driveId)
        assertTrue(reminders.cancelled.isEmpty())
    }

    private fun useCase(
        recorder: DriveRecorder = FakeDriveRecorder(),
        cache: FakeTripHistoryCache = FakeTripHistoryCache(),
        questions: FakeOpenDriveQuestion = FakeOpenDriveQuestion(),
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
    ) = DeletePastDriveUseCase(recorder, cache, questions, DriveReminders(reminders))

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

    private class FailingRemovalRecorder : DriveRecorder {
        override suspend fun record(drive: Drive) = Unit

        override suspend fun remove(id: String) = throw OfflineException(cause = null)
    }
}
